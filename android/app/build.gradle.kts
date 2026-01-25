// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

val APP_VERSION_NAME : String by project
val APP_VERSION_CODE : String by project
val APP_ID : String by project

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        targetSdk = libs.versions.target.sdk.version.get().toInt()
        namespace = APP_ID

        applicationId = APP_ID
        versionCode = APP_VERSION_CODE.toInt()
        versionName = APP_VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compilerextension.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }

    // Use this block to configure different flavors
//    flavorDimensions("version")
//    productFlavors {
//        create("full") {
//            dimension = "version"
//            applicationIdSuffix = ".full"
//        }
//        create("demo") {
//            dimension = "version"
//            applicationIdSuffix = ".demo"
//        }
//    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.libraryAndroid)
    implementation(projects.libraryCompose)
    implementation(projects.libraryKotlin)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.core.ktx)

    // WebView & Network
    implementation(libs.gson)
    implementation(libs.okhttp)

    // Compose BOM & Core
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
}
