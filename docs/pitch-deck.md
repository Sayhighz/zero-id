# ZeroID - Pitch Deck

> **"Prove who you are, without revealing who you are."**
>
> The first Digital Identity Wallet in Thailand powered by **Samsung Knox Vault** and **Zero-Knowledge Proofs**.

**งาน:** Samsung x KBTG Digital Fraud Cybersecurity Hackathon

**ทีม:**

- **Pratan Nilson** - Lead Architect & Cryptographer
- **Kunatip U-tong** - Mobile Security Engineer
- **Nonthee Panatuek** - Product Designer & AI Strategist

---

## 1. Problem: Data Breaches in Thailand

ปี 2023-2024 ข้อมูลส่วนตัวคนไทย **75 ล้าน records** ถูก leak (มากกว่าจำนวนประชากรทั้งประเทศ)

### สาเหตุ: ระบบ KYC แบบเดิม

ทุกครั้งที่ verify ตัวตน ต้อง copy ข้อมูลไปเก็บบน server ใหม่ ยิ่ง verify บ่อย ยิ่งมีสำเนาข้อมูลเยอะ ยิ่งเพิ่มจุดเสี่ยง

| สถานการณ์ | สิ่งที่ต้องทำ (แบบเดิม) | สิ่งที่จริงๆ ต้องการรู้ |
| --- | --- | --- |
| ซื้อเหล้า | โชว์บัตรประชาชน (เห็นชื่อ ที่อยู่ เลขบัตร) | "อายุเกิน 20 ไหม?" → ใช่/ไม่ใช่ |
| สมัครบัญชีธนาคาร | ถ่ายสำเนาบัตร → เก็บบน server | "เป็นคนไทยไหม?" → ใช่/ไม่ใช่ |
| กู้เงิน | ส่งข้อมูลรายได้ ที่อยู่ ทุกอย่าง | "รายได้เกิน 30,000 ไหม?" → ใช่/ไม่ใช่ |

---

## 2. Solution: ZeroID

ใช้ **Zero-Knowledge Proofs** พิสูจน์ตัวตนโดย **ไม่เปิดเผยข้อมูลส่วนตัวเลย**

```
แบบเดิม:
  User → ส่งชื่อ, เลขบัตร, วันเกิด, ที่อยู่ → Server เก็บทั้งหมด

แบบ ZeroID:
  User → ส่งแค่ Proof (หลักฐานทางคณิตศาสตร์) → Server รู้แค่ "ผ่าน/ไม่ผ่าน"
```

### Verifier เห็น vs ไม่เห็น

|  | แบบเดิม (KYC) | ZeroID |
| --- | --- | --- |
| อายุ >= 20? | เห็นวันเกิดจริง | เห็นแค่ "ใช่" |
| เป็นคนไทย? | เห็นเลขบัตร 13 หลัก | เห็นแค่ "ใช่" |
| ชื่อ-นามสกุล | เห็น | ไม่เห็น |
| ที่อยู่ | เห็น | ไม่เห็น |

---

## 3. Security 3 ชั้น

### ชั้นที่ 1: Samsung Knox Vault (Hardware Security)

- ข้อมูลส่วนตัว (วันเกิด, เลขบัตร) ถูกเก็บใน **Secure Chip** ของ Samsung
- แม้ root เครื่องก็ดึงข้อมูลออกไม่ได้
- ใช้ Android Keystore + StrongBox backing
- Private keys **non-extractable** จาก hardware

### ชั้นที่ 2: Zero-Knowledge Proofs (Cryptography)

- ใช้ **Circom** เขียน circuit logic
- ใช้ **snarkjs** + **Groth16** สร้างและ verify proof
- Proof ถูกสร้าง **locally บนมือถือ** ก่อนส่งไป server
- Server ไม่เคยเห็นข้อมูลจริงตลอดทั้ง process

### ชั้นที่ 3: KBTG AINU (Anti-Deepfake Biometric)

- ทุกครั้งที่ generate proof ต้องสแกนใบหน้าก่อน
- **iBeta Level 2 Certified** -- ป้องกัน deepfake
- ป้องกันบัญชีม้า / ขโมยมือถือมาใช้

---

## 4. How It Works

### System Architecture

```
Samsung Device (User)
├── Mobile App (Kotlin/Compose)
├── ZK Prover (snarkjs/WebView)
├── Samsung Knox Vault (Secure Chip)
└── AINU Liveness Check

External World
├── Verifier Node.js API
└── Mock Issuer
```

### Flow ทั้งระบบ

```
Step 1: Issuer ออก Credential
        กรมการปกครอง/ธนาคาร → ออกข้อมูลยืนยันตัวตน → เก็บใน Knox Vault

Step 2: Verifier ขอ Proof
        ร้านค้า/ธนาคาร สร้าง QR → "ขอพิสูจน์ว่าอายุ >= 20"

Step 3: User สแกน QR
        App อ่าน QR → แสดงว่า "ร้านนี้ขอดูอายุ"

Step 4: Liveness Check (AINU)
        สแกนใบหน้ายืนยันตัวตน → ป้องกัน deepfake

Step 5: Knox Vault ปลดล็อคข้อมูล
        ใบหน้าผ่าน → Knox ปล่อย private data (birthYear)

Step 6: Generate ZK Proof (บนมือถือ)
        snarkjs สร้าง Proof จาก birthYear โดยไม่ส่ง birthYear ออกไป

Step 7: ส่ง Proof ไป Verifier
        ส่งแค่ Proof + Public Signals (ไม่มีข้อมูลส่วนตัว)

Step 8: Verify
        Backend ตรวจ Proof → ตอบ "ผ่าน" หรือ "ไม่ผ่าน"
```

---

## 5. Working Prototype

### 5.1 ZK Circuits (2 เงื่อนไข)

#### Age Check Circuit

ตรวจสอบอายุโดยไม่เปิดเผยวันเกิด

- **Private input:** `birthYear` (ซ่อนใน Knox Vault)
- **Public input:** `minAge`, `currentYear`
- **Output:** `isOldEnough` (1 = ผ่าน, 0 = ไม่ผ่าน)
- **Logic:** `(currentYear - birthYear) >= minAge`

```circom
template AgeCheck() {
    signal input minAge;        // Public: เช่น 20
    signal input currentYear;   // Public: เช่น 2025
    signal input birthYear;     // Private: เช่น 2000 (ซ่อนใน Knox Vault)
    signal output isOldEnough;

    signal age;
    age <== currentYear - birthYear;    // 2025 - 2000 = 25

    component gte = GreaterEqThan(8);
    gte.in[0] <== age;                  // 25
    gte.in[1] <== minAge;               // 20
    isOldEnough <== gte.out;            // 1 (ผ่าน)
}
```

#### Thai Citizen ID Circuit

ตรวจสอบเลขบัตรประชาชนโดยไม่เปิดเผยเลขบัตร

- **Private input:** `idDigits[13]` (เลขบัตร 13 หลัก)
- **Output:** `isValid` (1 = ถูกต้อง, 0 = ไม่ถูก)
- **Logic:** Thai ID Checksum Algorithm (หลัก 1-12 คูณน้ำหนัก 13-2 แล้ว mod 11)

```circom
template ThaiCitizenCheck() {
    signal input idDigits[13];  // Private: เลขบัตร 13 หลัก
    signal output isValid;

    // Thai ID checksum: หลักที่ 1-12 คูณกับ 13-2 แล้วรวมกัน
    // ตรวจสอบว่า checkDigit ตรงกับหลักสุดท้าย
    // isValid = 1 ถ้าถูกต้อง
}
```

### 5.2 Backend Verifier API

Node.js + Express server สำหรับรับและ verify proofs

| Method | Endpoint | หน้าที่ |
| --- | --- | --- |
| GET | `/` | Health check |
| POST | `/api/verify` | Verify age proof |
| POST | `/api/verify-citizen` | Verify Thai citizen ID proof |

#### ผลลัพธ์ที่ทดสอบได้จริง

Age Verification:

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

Thai Citizen ID:

```json
{
  "success": true,
  "message": "Thai Citizen ID Verified",
  "details": {
    "isValidId": true
  }
}
```

### 5.3 Load Test

- Warm up: 30 วินาที @ 10 req/s
- Max load: 30 วินาที @ 100 req/s

### 5.4 Android App

- **Package:** `com.zero.id.app`
- **Tech:** Kotlin + Jetpack Compose
- **Target SDK:** 36 (Android 15)
- **Min SDK:** 23 (Android 6.0)
- **Architecture:** Multi-module (app, library-android, library-compose, library-kotlin)

### 5.5 UI Design (Figma)

4 หน้าจอหลัก:

1. **The Secure Wallet** -- หน้า Home แสดงบัตรประชาชนในรูปแบบดิจิทัล
2. **The Liveness Check** -- สแกนใบหน้าเพื่อปลดล็อค Knox Vault
3. **The Privacy Challenge** -- หน้าเลือกเงื่อนไขที่ต้องการ verify (อายุ > 20, เป็นคนไทย)
4. **Success & Privacy Statement** -- แสดงผลว่า verify สำเร็จ พร้อมสถิติ (Proof Size: 256 bytes)

---

## 6. Tech Stack

| Component | Technology | ทำไมถึงเลือก |
| --- | --- | --- |
| Mobile App | Kotlin + Jetpack Compose | Native performance, เข้าถึง hardware security ได้โดยตรง |
| Hardware Security | Android Keystore (StrongBox) | Interface กับ Samsung Knox Vault, hardware-level isolation |
| ZK Circuits | Circom 2.0 + snarkjs (Groth16) | Standard library สำหรับ ZK Proofs, verification เร็ว (constant time) |
| Proof System | Groth16 (BN128 curve) | Proof size เล็ก (~256 bytes), verify เร็ว |
| Backend | Node.js + Express | Lightweight, รองรับ async verification |
| Biometric | KBTG AINU | iBeta Level 2 certified, ป้องกัน deepfake |

---

## 7. Business Model

### Target Customer (B2B)

| Segment | Use Case | Value Proposition |
| --- | --- | --- |
| ธนาคาร / Fintech | KYC ไม่ต้องเก็บสำเนาบัตร | ลด data storage cost + PDPA risk |
| E-commerce | ยืนยันอายุ 18+/20+ | ลดภาระ compliance |
| ราชการ / DGA | Digital ID สำหรับบริการรัฐ | ลด data breach risk |
| ประกันภัย | ยืนยันตัวตนทำสัญญา | ลดค่าใช้จ่าย + ลดความเสี่ยง |
| Hospitality | เช็คอินโรงแรม, ร้านเหล้า | ไม่ต้องเก็บสำเนาบัตร |

### Revenue Streams

1. **API-as-a-Service (Per Verification)**
   - ค่า verify ครั้งละ 1-5 บาท
   - Volume discount สำหรับลูกค้าใหญ่
   - Monthly subscription plans

2. **Enterprise License**
   - On-premise deployment
   - Custom SLA

3. **Circuit Development**
   - สร้าง circuit ใหม่ตามเงื่อนไขลูกค้า
   - เช่น income check, credit score, membership

---

## 8. Market Opportunity

### แรงผลักดันตลาด

- **PDPA** (พ.ร.บ. คุ้มครองข้อมูลส่วนบุคคล) บังคับใช้แล้ว → ทุกบริษัทต้อง comply
- **ธปท.** ผลักดัน Digital KYC → เปิดโอกาสเข้าถึงตลาดธนาคาร
- **Digital Identity** เป็น trend ทั่วโลก (EU Digital Identity Wallet, India Aadhaar)

### Competitive Advantage

|  | ZeroID | Traditional KYC |
| --- | --- | --- |
| เก็บข้อมูลลูกค้า | ไม่เก็บ | เก็บบน server |
| ความเสี่ยง breach | ต่ำมาก (ไม่มีข้อมูลให้ leak) | สูง |
| PDPA compliance | ง่าย | ยาก (ต้อง manage consent) |
| Hardware security | Knox Vault | ไม่มี |
| Anti-deepfake | AINU (iBeta Level 2) | บางเจ้ามี/ไม่มี |
| Proof size | ~256 bytes | N/A |
| Verify speed | Constant time | N/A |

### ทำไม ZeroID ถึงต่างจากคนอื่น

- **Combine 3 ชั้นในตัวเดียว:** Hardware (Knox) + Crypto (ZK) + Biometric (AINU)
- **ยังไม่มีใครทำในไทย** ที่รวมทั้ง 3 อย่างเข้าด้วยกัน
- **Partner ที่แข็งแกร่ง:** Samsung (Knox) + KBTG (AINU)

---

## 9. Roadmap

| Phase | รายละเอียด | สถานะ |
| --- | --- | --- |
| Phase 0 | Architecture Design & Feasibility Study | สำเร็จ |
| Phase 1 | UI/UX Design (Figma) & Circuit Logic | สำเร็จ |
| Phase 2 | Knox Integration & End-to-End Flow (Hackathon) | กำลังทำ |
| Phase 3 | Production-ready API for KBTG Ecosystem | แผนอนาคต |

### สิ่งที่ทำเสร็จแล้ว

- 2 ZK Circuits (Age Check + Thai Citizen ID)
- Backend Verifier API (Node.js) + Load Test
- Android App skeleton (Kotlin + Compose)
- UI/UX Prototype (Figma)
- Working proof generation & verification

### แผนต่อไป

- Knox Vault integration บน Samsung device จริง
- AINU liveness check integration
- End-to-end flow: QR scan → liveness → proof → verify
- Production API with authentication & rate limiting

---

## 10. Q&A

### "ZK Proof ช้าไหม? Performance เป็นยังไง?"

Proof generation ใช้เวลาไม่กี่วินาทีบน mobile device Backend verification ทดสอบด้วย load test แล้วรับได้ 100 req/s เราใช้ Groth16 ซึ่งเป็น proof system ที่ verification เร็วมาก (constant time)

### "ถ้า Samsung Knox Vault ไม่มีในเครื่องล่ะ?"

Fallback ไปใช้ Android Keystore ปกติ ซึ่งยังมี TEE (Trusted Execution Environment) อยู่ แค่ security level ต่ำกว่า Knox Vault

### "ทำไมไม่ใช้ Blockchain?"

เราเลือกไม่ใช้ blockchain เพราะ verification ต้องเร็วและราคาถูก การ verify ZK Proof บน centralized server ใช้ต้นทุนต่ำกว่ามาก แต่สถาปัตยกรรมเราเปิดให้ integrate กับ blockchain ได้ในอนาคตถ้าต้องการ

### "Trusted Setup มีความเสี่ยงไหม?"

Groth16 ต้องมี trusted setup 1 ครั้งต่อ circuit ถ้าอนาคตเป็น concern สามารถย้ายไปใช้ PLONK หรือ STARKs ที่ไม่ต้อง trusted setup ได้

### "คู่แข่งเป็นใคร?"

ตลาด Digital ID มีหลายเจ้า แต่ ZeroID แตกต่างที่ combine 3 ชั้น: Hardware Security (Knox) + ZK Proofs + Anti-Deepfake (AINU) ในตัวเดียว ซึ่งยังไม่มีใครทำในไทย

### "Revenue model scale ได้ไหม?"

ได้ เพราะเป็น API-based ทุกครั้งที่มีการ verify = มี revenue ยิ่งลูกค้า (ธนาคาร, fintech) มี transaction มาก ยิ่ง scale สร้าง circuit ใหม่ตามเงื่อนไขก็เป็นอีก revenue stream

### "เพิ่มเงื่อนไขใหม่ยากไหม?"

สร้าง circuit ใหม่ + compile + trusted setup + เพิ่ม API endpoint แต่ละเงื่อนไขเป็นอิสระต่อกัน ตัวอย่างเงื่อนไขที่สามารถเพิ่มได้:

- Income Check (รายได้ >= X บาท)
- Credit Score Check (คะแนนเครดิต >= X)
- Location Check (อยู่ในพื้นที่ที่กำหนด)
- Membership Duration (เป็นสมาชิกมานาน >= X วัน)
- Expiry Check (บัตรยังไม่หมดอายุ)

---

## 11. Ask / Call to Action

### สิ่งที่ต้องการ

- Partnership กับ Samsung / KBTG เพื่อ pilot
- เข้าถึง Knox Vault SDK เต็มรูปแบบ
- ทดสอบกับ use case จริง (เช่น KBank app)

### Vision

> **"ทุกการยืนยันตัวตนในไทย ไม่ต้องเปิดเผยข้อมูลส่วนตัวอีกต่อไป"**
