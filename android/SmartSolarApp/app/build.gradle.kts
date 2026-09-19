/*
 * Smart Solar Microgrid Trading System
 * App-Level Build Configuration
 *
 * Member 2 - Native Android Prosumer Application
 *
 * This module configures the Android application with:
 * - SDK versions (min 24, target/compile 34)
 * - All dependencies for REST API (Retrofit/OkHttp), local DB (SQLite),
 *   Google Maps, QR code (ZXing), and Material Design UI components.
 */

plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.smartsolar.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartsolar.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

secrets {
    // Secrets plugin reads MAPS_API_KEY from local.properties
    propertiesFileName = "local.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {

    // ---------------------------------------------------------------
    // AndroidX Core & UI
    // ---------------------------------------------------------------
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ---------------------------------------------------------------
    // Material Design
    // ---------------------------------------------------------------
    implementation("com.google.android.material:material:1.12.0")

    // ---------------------------------------------------------------
    // Networking - Retrofit + OkHttp (REST API communication)
    // ---------------------------------------------------------------
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ---------------------------------------------------------------
    // JSON Parsing
    // ---------------------------------------------------------------
    implementation("com.google.code.gson:gson:2.11.0")

    // ---------------------------------------------------------------
    // Google Maps & Location Services
    // ---------------------------------------------------------------
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ---------------------------------------------------------------
    // QR Code - ZXing (Generation & Scanning)
    // ---------------------------------------------------------------
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // ---------------------------------------------------------------
    // Image Loading (for QR display and profile images)
    // ---------------------------------------------------------------
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ---------------------------------------------------------------
    // Testing
    // ---------------------------------------------------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
