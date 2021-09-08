import org.gradle.api.artifacts.dsl.DependencyHandler

object Versions {
    const val mockkVersion = "1.12.0"
    const val hiltVersion = "2.38.1"
    const val junitVersion = "4.13.2"
    const val timberVersion = "5.0.1"

    fun DependencyHandler.hilt() {
        add("implementation", "com.google.dagger:hilt-android:$hiltVersion")
        add("kapt", "com.google.dagger:hilt-compiler:$hiltVersion")

        add("androidTestImplementation", "com.google.dagger:hilt-android-testing:$hiltVersion")
        add("kaptAndroidTest", "com.google.dagger:hilt-compiler:$hiltVersion")

        add("testImplementation", "com.google.dagger:hilt-android-testing:$hiltVersion")
        add("kaptTest", "com.google.dagger:hilt-compiler:$hiltVersion")
    }
}