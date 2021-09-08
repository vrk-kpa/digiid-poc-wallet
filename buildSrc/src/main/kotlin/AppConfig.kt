import org.gradle.api.JavaVersion

object AppConfig {
    val javaVersion: JavaVersion = JavaVersion.VERSION_1_8

    const val compileSdk = 31
    const val minSdk = 26
    const val targetSdk = 31

}