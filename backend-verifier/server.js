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
let vkey;
try {
    vkey = JSON.parse(fs.readFileSync(vkeyPath, 'utf8'));
} catch (err) {
    console.error("Error loading verification_key.json:", err.message);
    process.exit(1); // ปิดโปรแกรมถ้าไม่มี Key หลัก
}

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

// โหลด Thai Citizen verification key
const thaiVkeyPath = path.join(__dirname, 'thai_citizen_vkey.json');
let thaiVkey;
try {
    thaiVkey = JSON.parse(fs.readFileSync(thaiVkeyPath, 'utf8'));
} catch (err) {
    console.warn("Warning: thai_citizen_vkey.json not found. Citizen verification will fail.");
}

// Thai Citizen verify endpoint
app.post('/api/verify-citizen', async (req, res) => {
    console.log("Received Thai Citizen Verification Request");
    
    try {
        const { proof, publicSignals } = req.body;

        if (!thaiVkey) {
            return res.status(500).json({
                success: false,
                message: "Server configuration error: Missing Thai Citizen Key"
            });
        }

        if (!proof || !publicSignals) {
            return res.status(400).json({ 
                success: false, 
                message: "Missing proof or publicSignals" 
            });
        }

        // Debugging: Log signal counts to check for mismatches
        console.log(`[Thai Citizen] Expected signals: ${thaiVkey.nPublic}, Received: ${publicSignals.length}`);

        const isValid = await snarkjs.groth16.verify(thaiVkey, publicSignals, proof);

        if (isValid) {
            res.json({ 
                success: true, 
                message: "Thai Citizen ID Verified",
                details: {
                    isValidId: publicSignals[0] === "1"
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

// โหลด Salary Check verification key
const salaryVkeyPath = path.join(__dirname, 'salary_check_vkey.json');
const salaryVkey = JSON.parse(fs.readFileSync(salaryVkeyPath, 'utf8'));

// Salary Check verify endpoint
app.post('/api/verify-salary', async (req, res) => {
    console.log("Received Salary Verification Request");

    try {
        const { proof, publicSignals } = req.body;

        if (!proof || !publicSignals) {
            return res.status(400).json({
                success: false,
                message: "Missing proof or publicSignals"
            });
        }

        const isValid = await snarkjs.groth16.verify(salaryVkey, publicSignals, proof);

        if (isValid) {
            res.json({
                success: true,
                message: "Salary Proof Verified",
                details: {
                    isAboveMin: publicSignals[0] === "1",
                    minSalary: publicSignals[1]
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

app.listen(port, () => {
    console.log(`ZeroID Verifier running on http://localhost:${port}`);
});