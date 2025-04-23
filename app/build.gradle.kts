plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python") // ✅ Chaquopy plugin added
}

android {
    lint {
        abortOnError = false
    }

    namespace = "com.labactivity.lala"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.labactivity.lala"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Chaquopy config - fixed syntax
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        // Correctly formatted Chaquopy Python configuration
        chaquopy {
            defaultConfig {
                pip {
                    install("numpy")             // ✅ For math & arrays
                    install("sympy")             // ✅ For symbolic math
                    install("pandas")            // ✅ For tabular data & analysis
                    install("matplotlib")        // ✅ For plotting/visualization
                    install("requests")          // ✅ For making HTTP requests
                    install("flask")             // ✅ For micro web frameworks
                    install("regex")             // ✅ Advanced string pattern handling
                    install("python-dateutil")   // ✅ Better datetime handling
                    install("scikit-learn")      // ✅ For ML basics
                }
            }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
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

    // ✅ Firebase
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.1")
    implementation("androidx.cardview:cardview:1.0.0") // Latest pa rin ito
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}

// ✅ Apply Firebase Plugin
apply(plugin = "com.google.gms.google-services")