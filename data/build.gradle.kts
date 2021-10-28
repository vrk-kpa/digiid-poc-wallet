import Versions.hilt

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

    hilt()

    implementation("com.jakewharton.timber:timber:${Versions.timberVersion}")

    implementation("io.github.ehn-digital-green-development:base45:0.0.3")

    val jacksonVersion = "2.13.0"
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:9.15.2")

    val bouncyCastleVersion = "1.69"
    implementation("org.bouncycastle:bcprov-jdk15on:$bouncyCastleVersion")
    implementation("org.bouncycastle:bcpkix-jdk15on:$bouncyCastleVersion")
    implementation("org.bouncycastle:bcutil-jdk15on:$bouncyCastleVersion")

    implementation("androidx.security:security-crypto:1.0.0")

    api("com.squareup.okhttp3:okhttp:4.9.1")

    val retrofitVersion = "2.9.0"
    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.retrofit2:converter-jackson:$retrofitVersion")
    api("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    testImplementation("junit:junit:${Versions.junitVersion}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
}