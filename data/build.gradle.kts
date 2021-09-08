plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":domain"))

    implementation("com.jakewharton.timber:timber:${Versions.timberVersion}")

    implementation("io.github.ehn-digital-green-development:base45:0.0.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.12.5")

    api("com.squareup.okhttp3:okhttp:4.9.1")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-jackson:2.9.0")

    testImplementation("junit:junit:${Versions.junitVersion}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
}