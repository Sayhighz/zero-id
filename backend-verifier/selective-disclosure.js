const crypto = require('crypto');

// ============================================================
// Selective Disclosure Service
// ============================================================
// ใช้ Hashed-Based Selective Disclosure:
// - Issuer (กรมการปกครอง) ออก credential พร้อม hash แต่ละ field
// - User เลือกเปิดเผยเฉพาะ field ที่ต้องการ
// - Verifier (ยาม) ตรวจสอบว่า field ที่เปิดเผย hash ตรงกับที่ issuer sign
// ============================================================

const ISSUER_SECRET = process.env.ISSUER_SECRET || 'zeroid-issuer-secret-key-2026';

// สร้าง salt สำหรับแต่ละ field (ป้องกัน brute force)
function generateSalt() {
    return crypto.randomBytes(16).toString('hex');
}

// Hash field ด้วย salt
function hashField(fieldName, fieldValue, salt) {
    return crypto.createHmac('sha256', salt)
        .update(`${fieldName}:${fieldValue}`)
        .digest('hex');
}

// Issuer sign ทั้ง credential
function signCredential(hashes) {
    const payload = JSON.stringify(hashes);
    return crypto.createHmac('sha256', ISSUER_SECRET)
        .update(payload)
        .digest('hex');
}

// Verify issuer signature
function verifySignature(hashes, signature) {
    const expected = signCredential(hashes);
    return crypto.timingSafeEqual(
        Buffer.from(expected, 'hex'),
        Buffer.from(signature, 'hex')
    );
}

// ============================================================
// 1. Issue Credential (กรมการปกครอง ออก credential)
// ============================================================
function issueCredential(personalData) {
    const salts = {};
    const hashes = {};

    // Hash แต่ละ field แยกกัน
    for (const [key, value] of Object.entries(personalData)) {
        salts[key] = generateSalt();
        hashes[key] = hashField(key, value, salts[key]);
    }

    // Issuer sign รวม hashes ทั้งหมด
    const signature = signCredential(hashes);

    return {
        // เก็บใน Knox Vault ของ user
        credential: {
            data: personalData,   // ข้อมูลจริง (เก็บใน device เท่านั้น)
            salts: salts,         // salt แต่ละ field
            hashes: hashes,       // hash แต่ละ field
            signature: signature, // ลายเซ็น issuer
            issuer: 'Department of Provincial Administration',
            issuedAt: new Date().toISOString()
        }
    };
}

// ============================================================
// 2. Present Credential (user เลือกเปิดเผย field)
// ============================================================
function presentCredential(credential, fieldsToReveal) {
    const revealedData = {};
    const revealedSalts = {};
    const allHashes = credential.hashes;

    // เปิดเผยเฉพาะ field ที่เลือก
    for (const field of fieldsToReveal) {
        if (credential.data[field] !== undefined) {
            revealedData[field] = credential.data[field];
            revealedSalts[field] = credential.salts[field];
        }
    }

    return {
        // ส่งให้ verifier (ยาม)
        presentation: {
            revealedData: revealedData,     // ข้อมูลที่เลือกเปิด
            revealedSalts: revealedSalts,   // salt ของ field ที่เปิด
            allHashes: allHashes,           // hash ทั้งหมด (ไม่เปิดเผยข้อมูล)
            signature: credential.signature, // ลายเซ็น issuer
            issuer: credential.issuer
        }
    };
}

// ============================================================
// 3. Verify Credential (verifier ตรวจสอบ)
// ============================================================
function verifyPresentation(presentation) {
    const { revealedData, revealedSalts, allHashes, signature } = presentation;

    // ขั้นที่ 1: ตรวจ issuer signature
    let signatureValid;
    try {
        signatureValid = verifySignature(allHashes, signature);
    } catch {
        signatureValid = false;
    }

    if (!signatureValid) {
        return { valid: false, reason: 'Invalid issuer signature' };
    }

    // ขั้นที่ 2: ตรวจแต่ละ field ที่เปิดเผย ว่า hash ตรงกัน
    for (const [field, value] of Object.entries(revealedData)) {
        const recomputedHash = hashField(field, value, revealedSalts[field]);
        if (recomputedHash !== allHashes[field]) {
            return { valid: false, reason: `Field "${field}" has been tampered` };
        }
    }

    // ขั้นที่ 3: นับ field ที่ซ่อน
    const totalFields = Object.keys(allHashes).length;
    const revealedFields = Object.keys(revealedData).length;
    const hiddenFields = totalFields - revealedFields;

    return {
        valid: true,
        revealedData: revealedData,
        totalFields: totalFields,
        revealedFields: revealedFields,
        hiddenFields: hiddenFields,
        issuer: presentation.issuer
    };
}

module.exports = {
    issueCredential,
    presentCredential,
    verifyPresentation
};
