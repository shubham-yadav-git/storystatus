plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.sky.storystatus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sky.storystatus"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE") ?: System.getenv("ANDROID_KEYSTORE_PATH") ?: project.findProperty("KEYSTORE_FILE") as String? ?: project.findProperty("ANDROID_KEYSTORE_PATH") as String?
            if (keystorePath != null) {
                val f = file(keystorePath)
                if (f.exists()) {
                    storeFile = f
                    storePassword = System.getenv("KEYSTORE_PASSWORD") ?: System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as String?
                    keyAlias = System.getenv("KEY_ALIAS") ?: System.getenv("ANDROID_KEY_ALIAS") ?: project.findProperty("KEY_ALIAS") as String?
                    keyPassword = System.getenv("KEY_PASSWORD") ?: System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as String?
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let {
                if (it.storeFile != null && it.storeFile!!.exists()) {
                    signingConfig = it
                }
            }
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
    lint {
        checkReleaseBuilds = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.gson)
    implementation(libs.androidx.datastore.preferences)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}