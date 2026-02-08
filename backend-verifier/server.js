const express = require("express");
const cors = require("cors");
const snarkjs = require("snarkjs");
const fs = require("fs");
const path = require("path");
const { v4: uuidv4 } = require("uuid"); // เพิ่มตัวนี้ด้านบน

// สร้าง Memory Database ไว้เก็บ Request
const verificationRequests = {};

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

// --- ฟังก์ชันช่วยโหลด Key (เพื่อไม่ให้ Code รก) ---
const loadVKey = (fileName) => {
  const p = path.join(__dirname, fileName);
  try {
    return JSON.parse(fs.readFileSync(p, "utf8"));
  } catch (err) {
    console.warn(`Warning: ${fileName} not found.`);
    return null;
  }
};

// โหลด Verification Keys ทั้งหมด
const vkey = loadVKey("verification_key.json"); // สำหรับ Age + Salary รวมกัน
const thaiVkey = loadVKey("thai_citizen_vkey.json");

// Health check
app.get("/", (req, res) => {
  res.json({
    status: "ZeroID Verifier is running",
    version: "2.0 (Integrated)",
  });
});

/**
 * 🚀 Endpoint ใหม่: ตรวจสอบทั้ง อายุ และ เงินเดือน ในครั้งเดียว
 * (ใช้กับวงจร age_salary_check.circom)
 */
app.post('/api/verify-profile', async (req, res) => {
    try {
        const { proof, publicSignals } = req.body;

        // Log ดูข้อมูลที่ส่งเข้ามาจริง ๆ
        console.log("Proof received:", proof ? "Yes" : "No");
        console.log("Signals received:", publicSignals);

        if (!proof || !publicSignals || !Array.isArray(publicSignals)) {
            return res.status(400).json({ success: false, message: "Invalid payload format" });
        }

        // เช็คจำนวน Signal ให้ตรงกับ Key
        if (publicSignals.length !== vkey.nPublic) {
            return res.status(400).json({ 
                success: false, 
                message: `Signal count mismatch. Expected ${vkey.nPublic}, got ${publicSignals.length}` 
            });
        }

        const isValid = await snarkjs.groth16.verify(vkey, publicSignals, proof);

        if (isValid) {
            res.json({ 
                success: true, 
                details: {
                    isQualified: publicSignals[0] === "1",
                    minAge: publicSignals[1],
                    minSalary: publicSignals[2],
                    currentYear: publicSignals[3]
                }
            });
        } else {
            res.status(401).json({ success: false, message: "Invalid Proof" });
        }
    } catch (error) {
        console.error("Verification Error Detail:", error);
        res.status(500).json({ success: false, error: "Verification process failed", detail: error.message });
    }
});

/**
 * 🚀 Endpoint สำหรับตรวจสอบทันทีโดยไม่ต้องมี requestId
 * ใช้สำหรับหน้าจอที่ต้องการผลลัพธ์ Pass/Fail ทันทีในแอป
 */
app.post('/api/verify-direct', async (req, res) => {
    try {
        const { proof, publicSignals } = req.body;

        // ตรวจสอบความครบถ้วนของข้อมูล
        if (!proof || !publicSignals) {
            return res.status(400).json({ success: false, message: "Missing proof or signals" });
        }

        // ตรวจสอบความถูกต้องทางคณิตศาสตร์ด้วย vkey
        const isValid = await snarkjs.groth16.verify(vkey, publicSignals, proof);

        if (isValid) {
            // ในวงจร age_salary_check:
            // publicSignals[0] มักจะเป็นผลลัพธ์ (1 = ผ่านเงื่อนไขทั้งหมด, 0 = ไม่ผ่าน)
            const isQualified = publicSignals[0] === "1";

            console.log("Direct Verification Result:", isQualified ? "Passed" : "Failed");
            
            res.json({ 
                success: true, 
                isQualified: isQualified,
                message: isQualified ? "Verification Passed" : "Verification Failed (Criteria not met)"
            });
        } else {
            res.status(401).json({ success: false, message: "Invalid Mathematical Proof" });
        }
    } catch (error) {
        console.error("Verification Error:", error);
        res.status(500).json({ success: false, error: error.message });
    }
});

/**
 * 🇹🇭 Thai Citizen ID Verify
 */
app.post("/api/verify-citizen", async (req, res) => {
  try {
    const { proof, publicSignals } = req.body;
    if (!thaiVkey) throw new Error("Citizen key not found");

    const isValid = await snarkjs.groth16.verify(
      thaiVkey,
      publicSignals,
      proof,
    );
    res.json({
      success: isValid,
      message: isValid ? "Thai Citizen ID Verified" : "Invalid Citizen Proof",
      isValidId: isValid && publicSignals[0] === "1",
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.get("/api/generate-challenge", (req, res) => {
    const requestId = uuidv4(); // สร้าง ID สุ่มไม่ให้ซ้ำกัน
    
    const challenge = {
        verifierName: "ZeroID Bank",
        minAge: 20,
        minSalary: 15000,
        currentYear: 2026,
        // สำคัญ: ต้องเป็น IP เครื่องคุณเพื่อให้ Android ยิงหาเจอ
        callbackUrl: `http://localhost:3000/api/verify-callback` 
    };



    res.json(challenge);
});

app.post('/api/verify-callback', async (req, res) => {
    try {
        const { requestId, proof, publicSignals } = req.body;

        // 1. เช็คว่ามี Request นี้จริงไหม
        const requestData = verificationRequests[requestId];
        if (!requestData) {
            return res.status(404).json({ success: false, message: "Request ID not found" });
        }

        // 2. ตรวจสอบ Proof (ใช้ logic เดิมที่คุณมี)
        const isValid = await snarkjs.groth16.verify(vkey, publicSignals, proof);

        if (isValid) {
            const isQualified = publicSignals[0] === "1";
            
            // 3. อัปเดตสถานะในระบบ
            verificationRequests[requestId].status = "COMPLETED";
            verificationRequests[requestId].result = isQualified ? "PASSED" : "FAILED";

            res.json({ success: true, isQualified });
        } else {
            verificationRequests[requestId].status = "FAILED";
            res.status(401).json({ success: false, message: "Invalid Proof" });
        }
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.get("/api/check-status/:requestId", (req, res) => {
    const data = verificationRequests[req.params.requestId];
    if (!data) return res.status(404).json({ message: "Not found" });
    res.json({ status: data.status, result: data.result });
});

app.listen(port, () => {
  console.log(`✅ ZeroID Verifier running on http://localhost:${port}`);
  if (vkey)
    console.log(
      `📋 Profile Key (Age+Salary) Loaded: Expected signals ${vkey.nPublic}`,
    );
});
