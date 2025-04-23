// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// In top-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")  // Gradle Plugin
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.chaquo.python:gradle:16.0.0")  // ✅ Latest Google Services
    }
}
