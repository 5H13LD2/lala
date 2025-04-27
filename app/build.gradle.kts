plugins {
    id("com.chaquo.python") // Ensure this is at the top
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
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

        // Chaquopy config
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }


    }

    // Python configuration
    chaquopy {
        defaultConfig {
            ("C:/Users/jerico/AppData/Local/Programs/Python/Python312/python.exe")

            pip {
                install("numpy")
                install("sympy")
                install("pandas")
                install("matplotlib")
                install("requests")
                install("flask")
                install("regex")
                install("python-dateutil")
                install("scikit-learn")
            }

            sourceSets {
                getByName("main") {
                    srcDir("src/main/python")
                }
            }
            sourceSets.getByName("main") {
                setSrcDirs(listOf("src/main/python"))
            }
        }

        // Rest of your configuration...
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

        // Firebase
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("com.google.firebase:firebase-firestore:24.10.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.google.android.gms:play-services-auth:21.0.0")
    }
}
