plugins {
    id("com.chaquo.python")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
}

// Chaquopy configuration - MUST be after plugins and before android {}
chaquopy {
    defaultConfig {
        pip {
            install("numpy")
        }
    }
    sourceSets {
        getByName("main") {
            setSrcDirs(listOf("src/main/python"))
        }
    }
}

android {
    lint {
        abortOnError = false
    }

    namespace = "com.labactivity.lala"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.labactivity.lala"
        minSdk = 26  // Increased from 24 to support Kotlin Scripting and JRuby
        targetSdk = 35

        // Auto-increment version based on git commits
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable MultiDex for Janino
        multiDexEnabled = true

        // Chaquopy NDK config
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // Packaging options to handle duplicate files
    packaging {
        resources {
            // Exclude duplicate Kotlin builtin files from kotlin-compiler-embeddable and kotlin-stdlib
            excludes += setOf(
                "META-INF/*.kotlin_module",
                "META-INF/kotlin/**",
                "kotlin/**/*.kotlin_builtins",
                "kotlin/kotlin.kotlin_builtins",
                "kotlin/coroutines/coroutines.kotlin_builtins",
                "kotlin/internal/internal.kotlin_builtins",
                "kotlin/reflect/reflect.kotlin_builtins",
                "kotlin/ranges/ranges.kotlin_builtins",
                "kotlin/annotation/annotation.kotlin_builtins",
                "kotlin/collections/collections.kotlin_builtins"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Firebase
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("com.google.firebase:firebase-firestore:24.10.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.google.android.gms:play-services-auth:21.0.0")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

        //SQL COMPILER
        implementation ("androidx.core:core-ktx:1.12.0")
        implementation ("androidx.appcompat:appcompat:1.6.1")
        implementation ("com.google.android.material:material:1.10.0")
        implementation ("com.google.code.gson:gson:2.10.1")
        testImplementation ("junit:junit:4.13.2")
        androidTestImplementation ("androidx.test.ext:junit:1.1.5")
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

        // CircleIndicator for RecyclerView dots indicator
        implementation ("me.relex:circleindicator:2.1.6")

        // Janino - Java Compiler for Android
        implementation ("org.codehaus.janino:janino:3.1.10")
        implementation ("org.codehaus.janino:commons-compiler:3.1.10")

        // MultiDex support
        implementation ("androidx.multidex:multidex:2.0.1")

        // ============================================
        // UNIFIED COMPILER DEPENDENCIES
        // ============================================

        // Kotlin Scripting - for KotlinCompiler
        implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.9.22")
        implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.9.22")
        implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.22")
        implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.9.22")
        implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.9.22")

        // Ruby - JRuby for RubyCompiler
        implementation("org.jruby:jruby-complete:9.4.5.0")

        // JavaScript - Rhino for future JavaScript support
        implementation("org.mozilla:rhino:1.7.14")
    }
}
dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Firebase Functions - only needed for FeedbackDialogWithBackendEmail (Cloud Functions)
    // implementation(libs.firebase.functions.ktx)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

/**
 * Get version code based on git commit count
 * This ensures consistent versioning across all builds from the same commit
 */
fun getVersionCode(): Int {
    return try {
        val stdout = java.io.ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim().toIntOrNull() ?: 1
    } catch (e: Exception) {
        println("Warning: Could not get git commit count: ${e.message}")
        1 // Default version code
    }
}

/**
 * Get version name based on git tag and commit
 * Format: vX.Y.Z or vX.Y.Z-commitHash if no tag
 */
fun getVersionName(): String {
    return try {
        val tag = getLatestGitTag()
        if (tag.isNotEmpty()) {
            tag
        } else {
            // No tag, use commit hash
            val commitHash = getGitCommitHash()
            "1.0.0-$commitHash"
        }
    } catch (e: Exception) {
        println("Warning: Could not get version name: ${e.message}")
        "1.0.0" // Default version name
    }
}

/**
 * Get the latest git tag
 */
fun getLatestGitTag(): String {
    return try {
        val stdout = java.io.ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get the current git commit hash (short)
 */
fun getGitCommitHash(): String {
    return try {
        val stdout = java.io.ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        "unknown"
    }
}
