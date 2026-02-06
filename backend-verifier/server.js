const express = require('express');
const cors = require('cors');
const snarkjs = require('snarkjs');
const fs = require('fs');
const path = require('path');
const { issueCredential, presentCredential, verifyPresentation } = require('./selective-disclosure');

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

// โหลด Thai Citizen verification key
const thaiVkeyPath = path.join(__dirname, 'thai_citizen_vkey.json');
const thaiVkey = JSON.parse(fs.readFileSync(thaiVkeyPath, 'utf8'));

// Thai Citizen verify endpoint
app.post('/api/verify-citizen', async (req, res) => {
    console.log("Received Thai Citizen Verification Request");
    
    try {
        const { proof, publicSignals } = req.body;

        if (!proof || !publicSignals) {
            return res.status(400).json({ 
                success: false, 
                message: "Missing proof or publicSignals" 
            });
        }

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

// ============================================================
// Selective Disclosure Endpoints (เข้าหมู่บ้าน)
// ============================================================

// 1. Issue Credential - กรมการปกครอง ออก credential ให้ user
app.post('/api/credential/issue', (req, res) => {
    console.log("Received Issue Credential Request");

    try {
        const { personalData } = req.body;

        if (!personalData) {
            return res.status(400).json({
                success: false,
                message: "Missing personalData"
            });
        }

        const result = issueCredential(personalData);

        res.json({
            success: true,
            message: "Credential Issued",
            credential: result.credential
        });
    } catch (error) {
        console.error("Issue error:", error);
        res.status(500).json({
            success: false,
            message: "Failed to issue credential",
            error: error.message
        });
    }
});

// 2. Present Credential - user เลือก field ที่จะเปิดเผย
app.post('/api/credential/present', (req, res) => {
    console.log("Received Present Credential Request");

    try {
        const { credential, fieldsToReveal } = req.body;

        if (!credential || !fieldsToReveal) {
            return res.status(400).json({
                success: false,
                message: "Missing credential or fieldsToReveal"
            });
        }

        const result = presentCredential(credential, fieldsToReveal);

        res.json({
            success: true,
            message: "Presentation Created",
            presentation: result.presentation
        });
    } catch (error) {
        console.error("Present error:", error);
        res.status(500).json({
            success: false,
            message: "Failed to create presentation",
            error: error.message
        });
    }
});

// 3. Verify Credential - ยาม/verifier ตรวจสอบ
app.post('/api/credential/verify', (req, res) => {
    console.log("Received Verify Credential Request");

    try {
        const { presentation } = req.body;

        if (!presentation) {
            return res.status(400).json({
                success: false,
                message: "Missing presentation"
            });
        }

        const result = verifyPresentation(presentation);

        if (result.valid) {
            res.json({
                success: true,
                message: "Credential Verified - Data is authentic",
                details: result
            });
        } else {
            res.json({
                success: false,
                message: "Credential Verification Failed",
                reason: result.reason
            });
        }
    } catch (error) {
        console.error("Verify error:", error);
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