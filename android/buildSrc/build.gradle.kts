plugins {
    `kotlin-dsl`
}
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.kgp)
    implementation(libs.agp)

    // 1. Retrofit สำหรับเชื่อมต่อ API Backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 2. ZXing สำหรับสแกน QR Code (หน้าที่ของ Night)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // 3. สำหรับทำงานกับ Coroutines (ใช้เรียก API แบบ Async)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

kotlin {
    jvmToolchain(17)
}