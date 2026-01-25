# ZeroID Verifier API

Backend API สำหรับ verify Zero-Knowledge Proofs

## 🚀 วิธีรัน
```bash
cd backend-verifier
npm install
node server.js
```

Server จะรันที่ `http://localhost:3000`

---

## 📡 API Endpoints

### 1. Health Check
```
GET /
```

**Response:**
```json
{"status": "ZeroID Verifier is running"}
```

---

### 2. Verify Age Proof
```
POST /api/verify
```

**Body:**
```json
{
  "proof": { ... },
  "publicSignals": ["1", "20", "2025"]
}
```

**Response (สำเร็จ):**
```json
{
  "success": true,
  "message": "Zero-Knowledge Proof Verified",
  "details": {
    "isOldEnough": true,
    "minAge": "20",
    "currentYear": "2025"
  }
}
```

**Response (ไม่สำเร็จ):**
```json
{
  "success": false,
  "message": "Invalid Proof"
}
```

---

### 3. Verify Thai Citizen ID
```
POST /api/verify-citizen
```

**Body:**
```json
{
  "proof": { ... },
  "publicSignals": ["1"]
}
```

**Response (สำเร็จ):**
```json
{
  "success": true,
  "message": "Thai Citizen ID Verified",
  "details": {
    "isValidId": true
  }
}
```

---

## 📂 ไฟล์สำคัญ

| ไฟล์ | คำอธิบาย |
|------|---------|
| `server.js` | Express server หลัก |
| `verification_key.json` | Key สำหรับ verify age proof |
| `thai_citizen_vkey.json` | Key สำหรับ verify Thai ID proof |

---

## 🧪 ทดสอบด้วย curl
```bash
# Health check
curl http://localhost:3000/

# Verify age (ต้องมี proof.json และ public.json)
curl -X POST http://localhost:3000/api/verify \
  -H "Content-Type: application/json" \
  -d '{"proof": {...}, "publicSignals": ["1","20","2025"]}'
```

---

## 👨‍💻 ผู้พัฒนา

Few - Backend Developer + ZK Circuits