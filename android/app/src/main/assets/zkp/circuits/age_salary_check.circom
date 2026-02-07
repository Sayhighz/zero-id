pragma circom 2.0.0;

template GreaterEqThan(n) {
    signal input in[2];
    signal output out;
    signal bits[n + 1]; // ต้องใช้ n + 1 บิต
    var sum = (1 << n) + in[0] - in[1];
    for (var i = 0; i <= n; i++) {
        bits[i] <-- (sum >> i) & 1;
        bits[i] * (bits[i] - 1) === 0;
    }
    
    // ตรวจสอบความถูกต้องของการแยกบิต
    var sum_check = 0;
    for (var i = 0; i <= n; i++) {
        sum_check += bits[i] * (1 << i);
    }
    sum === sum_check;
    
    out <== bits[n]; // บิตที่ n คือผลลัพธ์ของการเปรียบเทียบ (1 คือ >=, 0 คือ <)
}

template AgeSalaryCheck() {
    signal input minAge;      
    signal input minSalary;   
    signal input currentYear; 
    signal input birthYear;   
    signal input salary;      
    signal output isQualified;

    signal age <== currentYear - birthYear;
    component ageGte = GreaterEqThan(8);
    ageGte.in[0] <== age;
    ageGte.in[1] <== minAge;

    component salaryGte = GreaterEqThan(32);
    salaryGte.in[0] <== salary;
    salaryGte.in[1] <== minSalary;

    isQualified <== ageGte.out * salaryGte.out;
}

component main {public [minAge, minSalary, currentYear]} = AgeSalaryCheck();