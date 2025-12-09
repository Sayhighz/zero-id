pragma circom 2.0.0;

template AgeCheck() {
    // Public inputs (Visible to Verifier)
    signal input minAge;
    signal input currentYear;

    // Private inputs (Hidden in Knox Vault)
    signal input birthYear;

    // Output (Result)
    signal output isOldEnough;

    // Constraint Logic
    var age = currentYear - birthYear;
    isOldEnough <== age >= minAge;
}

component main = AgeCheck();