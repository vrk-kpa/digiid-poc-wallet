import Versions.hilt
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "fi.dvv.digiid.poc.wallet"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        properties.load(FileInputStream("local.properties"))

        buildConfigField(
            "String",
            "WALLET_BASE_URL",
            "\"${properties.getProperty("walletBaseUrl")}\""
        )

        buildConfigField("String", "WALLET_CERTIFICATE_PEM",
            properties.getProperty("walletCertificatePEM").lines()
                .joinToString(" +\n") { "\"$it\\n\"" })

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    hilt()
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val navigationVersion = "2.3.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    androidTestImplementation("androidx.navigation:navigation-testing:$navigationVersion")
}

dependencies {
    val lifecycleVersion = "2.4.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")

    implementation("com.google.zxing:core:3.4.1")

    implementation("androidx.camera:camera-camera2:1.1.0-alpha10")
    implementation("androidx.camera:camera-lifecycle:1.1.0-alpha10")
    implementation("androidx.camera:camera-view:1.0.0-alpha30")

    implementation("com.jakewharton.timber:timber:${Versions.timberVersion}")

    testImplementation("junit:junit:${Versions.junitVersion}")
    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

dependencies {
    implementation(project(":data"))
}