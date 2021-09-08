import java.util.Properties
import java.io.FileInputStream

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/vrk-kpa/digiid-poc-wallet")

            val properties = Properties().apply {
                load(FileInputStream("local.properties"))
            }

            credentials {
                username = properties.getProperty("githubUsername")
                password = properties.getProperty("githubToken")
            }
        }

        google()
        mavenCentral()
    }
}
rootProject.name = "Digi-ID"
include(":app")
include(":domain")
include(":data")
