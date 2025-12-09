const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());

// Mock Verification Endpoint
app.post('/api/verify', (req, res) => {
    console.log("Received Proof Verification Request");
    const { proof, publicSignals } = req.body;

    // TODO: Implement snarkjs.groth16.verify here
    // For Hackathon Prototype: Return TRUE
    
    setTimeout(() => {
        res.json({ 
            success: true, 
            message: "Zero-Knowledge Proof Verified",
            details: {
                isThaiCitizen: true,
                ageCheck: "PASS"
            }
        });
    }, 1000);
});

app.listen(port, () => {
  console.log(`ZeroID Verifier running on port ${port}`);
});