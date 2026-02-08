@echo off
echo --- 1. Compiling Circuit ---
call circom age_salary_check.circom --r1cs --wasm --sym

echo --- 2. Trusted Setup (Groth16) ---
:: เตรียม Phase 2
call snarkjs powersoftau prepare phase2 pot12_0001.ptau prepared_pot12.ptau -v
:: Setup zkey เริ่มต้น
call snarkjs groth16 setup age_salary_check.r1cs prepared_pot12.ptau circuit_0000.zkey
:: Contribute (ใส่ entropy)
call snarkjs zkey contribute circuit_0000.zkey circuit_final.zkey --name="Final_Key" -v -e="random_entropy_123"
:: Export Verification Key
call snarkjs zkey export verificationkey circuit_final.zkey verification_key.json

echo --- 3. Generating Witness ---
node age_salary_check_js/generate_witness.js age_salary_check_js/age_salary_check.wasm input.json witness.wtns

echo --- 4. Generating Proof ---
call snarkjs groth16 prove circuit_final.zkey witness.wtns proof.json public.json

echo --- 5. Verifying Proof Locally ---
call snarkjs groth16 verify verification_key.json public.json proof.json

echo --- 6. Public Signals (Result) ---
type public.json
echo.
echo --- Done ---
