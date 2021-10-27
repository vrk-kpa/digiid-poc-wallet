plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = AppConfig.javaVersion
    targetCompatibility = AppConfig.javaVersion
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("fi.dvv.digiid.poc.vc:digiid-poc-verifiable-credentials:0.0.6")
    api("com.squareup.okhttp3:okhttp-tls:4.9.1")
}