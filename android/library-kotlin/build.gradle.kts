plugins {
    id("java-library")
    kotlin("jvm")
    id("maven-publish")
    publish
}

dependencies {
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(17)
}
