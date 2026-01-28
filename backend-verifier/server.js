const express = require('express');
const cors = require('cors');
const snarkjs = require('snarkjs');
const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

/* =========================
   In-memory Session Store
   ========================= */
const sessions = {};

/* =========================
   Load Verification Keys
   ========================= */
const vkey = JSON.parse(
  fs.readFileSync(path.join(__dirname, 'verification_key.json'), 'utf8')
);

/* =========================
   Health Check
   ========================= */
app.get('/', (req, res) => {
  res.json({ status: 'ZeroID Verifier is running' });
});

/* =========================
   Create Session
   ========================= */
app.post('/api/session/create', (req, res) => {
  const sessionId = uuidv4();

  sessions[sessionId] = {
    verified: false,
    userData: null,
    createdAt: new Date()
  };

  res.json({ sessionId });
});

/* =========================
   Submit User Data
   ========================= */
app.post('/api/session/:sessionId/submit', (req, res) => {
  const { sessionId } = req.params;
  const userData = req.body;

  if (!sessions[sessionId]) {
    return res.status(404).json({ message: 'Session not found' });
  }

  sessions[sessionId].userData = userData;

  res.json({ success: true });
});

/* =========================
   Get Session Data
   ========================= */
app.get('/api/session/:sessionId', (req, res) => {
  const { sessionId } = req.params;

  const session = sessions[sessionId];
  if (!session) {
    return res.status(404).json({ message: 'Session not found' });
  }

  res.json({
    ...session.userData,
    verified: session.verified
  });
});

/* =========================
   Verify ZK Proof
   ========================= */
app.post('/api/verify', async (req, res) => {
  try {
    const { proof, publicSignals, sessionId } = req.body;

    if (!proof || !publicSignals || !sessionId) {
      return res.status(400).json({
        success: false,
        message: 'Missing proof, publicSignals, or sessionId'
      });
    }

    if (!sessions[sessionId]) {
      return res.status(404).json({
        success: false,
        message: 'Session not found'
      });
    }

    const isValid = await snarkjs.groth16.verify(
      vkey,
      publicSignals,
      proof
    );

    if (!isValid) {
      return res.json({
        success: false,
        message: 'Invalid Proof'
      });
    }

    // ตัวอย่าง publicSignals
    const isOldEnough = publicSignals[0] === '1';

    sessions[sessionId].verified = isOldEnough;

    res.json({
      success: true,
      message: 'Zero-Knowledge Proof Verified',
      verified: isOldEnough
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Verification failed',
      error: error.message
    });
  }
});

/* =========================
   Start Server
   ========================= */
app.listen(port, () => {
  console.log(`ZeroID Backend running on http://localhost:${port}`);
});
