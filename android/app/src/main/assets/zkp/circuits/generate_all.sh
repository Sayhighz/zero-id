#!/bin/bash
# 1. Compile วงจร
circom age_salary_check.circom --r1cs --wasm --sym

# 2. Setup กุญแจ (ZKey)
snarkjs groth16 setup age_salary_check.r1cs pot12_0001.ptau circuit_0000.zkey
snarkjs zkey contribute circuit_0000.zkey circuit_final.zkey --name="First Contributor" -v -e="random_entropy"
snarkjs zkey export verificationkey circuit_final.zkey verification_key.json

# 3. สร้าง Proof
node age_salary_check_js/generate_witness.js age_salary_check_js/age_salary_check.wasm input.json witness.wtns
snarkjs groth16 prove circuit_final.zkey witness.wtns proof.json public.json

# 4. มัดรวมข้อมูลส่งให้ Frontend ในรูปแบบ QR Code
echo "{\"proof\": $(cat proof.json), \"publicSignals\": $(cat public.json)}" > final_data.json
qrencode -s 6 -l L -o proof_qr.png < final_data.json

echo "✅ สำเร็จ! ได้ไฟล์ proof_qr.png สำหรับ Frontend แล้ว"