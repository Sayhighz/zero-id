const express = require('express');
const cors = require('cors');
const snarkjs = require('snarkjs');
const fs = require('fs');
const path = require('path');

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

// โหลด verification key
const vkeyPath = path.join(__dirname, 'verification_key.json');
const vkey = JSON.parse(fs.readFileSync(vkeyPath, 'utf8'));

// Health check
app.get('/', (req, res) => {
    res.json({ status: 'ZeroID Verifier is running' });
});

// Verify endpoint
app.post('/api/verify', async (req, res) => {
    console.log("Received Proof Verification Request");
    
    try {
        const { proof, publicSignals } = req.body;

        if (!proof || !publicSignals) {
            return res.status(400).json({ 
                success: false, 
                message: "Missing proof or publicSignals" 
            });
        }

        // Verify proof ด้วย snarkjs
        const isValid = await snarkjs.groth16.verify(vkey, publicSignals, proof);

        if (isValid) {
            const isOldEnough = publicSignals[0] === "1";
            const minAge = publicSignals[1];
            const currentYear = publicSignals[2];

            res.json({ 
                success: true, 
                message: "Zero-Knowledge Proof Verified",
                details: {
                    isOldEnough: isOldEnough,
                    minAge: minAge,
                    currentYear: currentYear
                }
            });
        } else {
            res.json({ 
                success: false, 
                message: "Invalid Proof" 
            });
        }
    } catch (error) {
        console.error("Verification error:", error);
        res.status(500).json({ 
            success: false, 
            message: "Verification failed",
            error: error.message 
        });
    }
});

// แก้จากแบบเดิม เป็นแบบนี้เพื่อให้รับแขกจาก 10.0.2.2 ได้
app.listen(3001, '0.0.0.0', () => {
    console.log("ZeroID Verifier running on http://0.0.0.0:3000");
});