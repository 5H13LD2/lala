pluginManagement {
    repositories {
        google()
        mavenCentral()
        // Add this line to include the chaquopy repository
        maven("https://chaquo.com/maven")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add this line for the chaquopy plugin
        maven("https://chaquo.com/maven")
    }
}

rootProject.name = "lala"
include(":app")