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

// นี่คือตัวอย่างโค้ดฝั่งเซิร์ฟเวอร์ (เช่น Node.js)

// สร้าง Endpoint รอรับข้อมูลที่ /api/submit-data ด้วย method POST
app.post('/api/submit-data', (req, res) => {  // ข้อมูลที่แอปส่งมาจะอยู่ใน req.body
  const userData = req.body;

  // แสดงผลใน console ของเซิร์ฟเวอร์
  console.log('ได้รับข้อมูลผู้ใช้:', userData);

  // --- คุณสามารถเพิ่มตรรกะของคุณตรงนี้ ---
  // เช่น ตรวจสอบข้อมูล, บันทึกลง Database
  // ------------------------------------

  // เมื่อทำสำเร็จ ส่งสถานะ 200 OK กลับไปให้แอป
  res.status(200).send({ message: 'ได้รับข้อมูลเรียบร้อย' });
});

app.listen(port, () => {
    console.log(`ZeroID Verifier running on http://localhost:${port}`);
});