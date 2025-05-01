// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.9.2")  // Make sure this matches your Android Studio version
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.chaquo.python:gradle:16.0.0")
    }
}