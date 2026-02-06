package com.zero.id.app.model

/**
 * Data class representing user's private identity information
 */
data class UserProfile(
    val fullName: String = "Nonthee Panatuek",
    val birthDay: Int = 1,
    val birthMonth: Int = 2,
    val birthYear: Int = 1990,
    val address: String = "เลขที่ xx/xx ",
    val idNumber: String = "1100000000000",
    val phoneNumber: String = "0900000000",
    val salary: Int = 14000
) {
    /**
     * Helper to get full date of birth as string
     */
    fun getFormattedBirthDate(): String = "$birthDay/$birthMonth/$birthYear"
}
