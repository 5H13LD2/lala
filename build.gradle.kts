// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")  // Update Gradle version if needed
        classpath("com.google.gms:google-services:4.4.0")  // Firebase Plugin (Correct Placement)
        classpath ("com.google.gms:google-services:4.3.10")

    }

}
