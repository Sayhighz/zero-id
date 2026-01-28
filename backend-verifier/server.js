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

// --- 1. Pre-load Verification Keys ---
const vkeyPath = path.join(__dirname, 'verification_key.json');
const thaiVkeyPath = path.join(__dirname, 'thai_citizen_vkey.json');

if (!fs.existsSync(vkeyPath) || !fs.existsSync(thaiVkeyPath)) {
    console.error("Verification key not found!");
    process.exit(1);
}

const vkey = JSON.parse(fs.readFileSync(vkeyPath, 'utf8'));
const thaiVkey = JSON.parse(fs.readFileSync(thaiVkeyPath, 'utf8'));

// --- 2. In-memory Session Store (For development only) ---
const sessions = {};
const SESSION_EXPIRATION = 30 * 60 * 1000; // 30 minutes

// --- Health check ---
app.get('/', (req, res) => {
    res.json({ status: 'ZeroID Verifier is running' });
});

// --- 3. Zero-Knowledge Verification Endpoints ---

app.post('/api/verify', async (req, res) => {
    try {
        const { proof, publicSignals } = req.body;
        if (!proof || !publicSignals || !Array.isArray(publicSignals) || publicSignals.length !== 3) {
            return res.status(400).json({ success: false, message: "Invalid input: Missing or malformed proof or publicSignals" });
        }

        const isValid = await snarkjs.groth16.verify(vkey, publicSignals, proof);

        if (isValid) {
            res.json({
                success: true,
                message: "Zero-Knowledge Proof Verified",
                details: {
                    isOldEnough: publicSignals[0] === "1",
                    minAge: publicSignals[1],
                    currentYear: publicSignals[2]
                }
            });
        } else {
            res.status(401).json({ success: false, message: "Invalid Proof" });
        }
    } catch (error) {
        console.error("Verification error:", error);
        res.status(500).json({ success: false, message: "An internal error occurred during verification.", error: error.message });
    }
});

app.post('/api/verify-citizen', async (req, res) => {
    try {
        const { proof, publicSignals } = req.body;
        if (!proof || !publicSignals || !Array.isArray(publicSignals) || publicSignals.length !== 1) {
            return res.status(400).json({ success: false, message: "Invalid input: Missing or malformed proof or publicSignals" });
        }

        const isValid = await snarkjs.groth16.verify(thaiVkey, publicSignals, proof);

        if (isValid) {
            res.json({
                success: true,
                message: "Thai Citizen ID Proof Verified",
                details: {
                    isValidId: publicSignals[0] === "1"
                }
            });
        } else {
            res.status(401).json({ success: false, message: "Invalid Thai Citizen ID Proof" });
        }
    } catch (error) {
        console.error("Citizen verification error:", error);
        res.status(500).json({ success: false, message: "An internal error occurred during citizen verification.", error: error.message });
    }
});

// --- 4. Session Management ---

app.post('/api/session/create', (req, res) => {
    const sessionId = uuidv4();
    sessions[sessionId] = {
        data: {},
        createdAt: Date.now()
    };
    res.json({ sessionId });
});

app.post('/api/session/:sessionId/submit', (req, res) => {
    const { sessionId } = req.params;
    const userData = req.body;

    if (!sessions[sessionId]) {
        return res.status(404).json({ message: "Session not found or has expired" });
    }

    sessions[sessionId].data = userData;
    console.log(`Data submitted for session ${sessionId}:`, userData);
    res.status(200).send();
});

app.get('/api/session/:sessionId', (req, res) => {
    const { sessionId } = req.params;
    const session = sessions[sessionId];

    if (!session) {
        return res.status(404).json({ message: "Session not found or has expired" });
    }

    res.json(session.data);
});

// --- Session Cleanup ---
setInterval(() => {
    const now = Date.now();
    for (const sessionId in sessions) {
        if (now - sessions[sessionId].createdAt > SESSION_EXPIRATION) {
            delete sessions[sessionId];
            console.log(`Session ${sessionId} expired and cleaned up.`);
        }
    }
}, 60 * 1000); // Check every minute


app.listen(port, () => {
    console.log(`ZeroID Verifier running on http://localhost:${port}`);
});
