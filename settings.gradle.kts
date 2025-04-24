pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()  // Default Maven repository
        gradlePluginPortal()  // Official Gradle plugin portal
        // Add any custom repositories here, if necessary
    }
    plugins {
        // Add plugin versions here, like Kotlin, etc.
        kotlin("android") version "1.6.10"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Optionally add more repositories as needed
        maven {
            url = uri("https://chaquo.com/maven")
        }
    }
}

rootProject.name = "lala"  // Define the project name
include(":app")  // Include the 'app' module
