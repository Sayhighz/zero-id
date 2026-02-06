pragma circom 2.0.0;

// Comparator: ถ้า in[0] < in[1] → out = 1, ไม่งั้น out = 0
template LessThan(n) {
    signal input in[2];
    signal output out;

    signal bits[n];
    var sum = (1 << n) + in[1] - in[0];

    for (var i = 0; i < n; i++) {
        bits[i] <-- (sum >> i) & 1;
        bits[i] * (bits[i] - 1) === 0;
    }

    out <== 1 - bits[n-1];
}

// GreaterEqThan: ถ้า in[0] >= in[1] → out = 1
template GreaterEqThan(n) {
    signal input in[2];
    signal output out;

    component lt = LessThan(n);
    lt.in[0] <== in[0];
    lt.in[1] <== in[1];
    out <== 1 - lt.out;
}

template SalaryCheck() {
    // Public inputs (Verifier เห็น)
    signal input minSalary;

    // Private inputs (ซ่อนใน Knox Vault)
    signal input salary;

    // Output
    signal output isAboveMin;

    // เช็คว่า salary >= minSalary
    component gte = GreaterEqThan(20);  // รองรับค่า 0-1,048,575 บาท
    gte.in[0] <== salary;
    gte.in[1] <== minSalary;

    isAboveMin <== gte.out;
}

component main {public [minSalary]} = SalaryCheck();
