package com.zero.id.network

// ✅ แก้ให้ตรงกับที่ Backend ของเฟวรอรับ (req.body)
data class ProofRequest(
    val proof: Map<String, Any>,       // เก็บโครงสร้าง Proof จาก snarkjs
    val publicSignals: List<String>    // เก็บค่า Public Signals (เช่น [1, 18, 2026])
)

// ✅ แก้ให้ตรงกับที่ Backend ของเฟวส่งกลับมา (res.json)
data class ProofResponse(
    val success: Boolean,              // เฟวใช้ชื่อ success
    val message: String,
    val details: VerificationDetails? = null // ข้อมูลเพิ่มเติมที่เฟวส่งมา
)

data class VerificationDetails(
    val isOldEnough: Boolean,
    val minAge: String,
    val currentYear: String
)