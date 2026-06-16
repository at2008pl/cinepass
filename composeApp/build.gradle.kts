@file:Suppress("DEPRECATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Lifecycle Viewmodel & Runtime
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
            
            // Navigation
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha08")
            
            // Ktor HTTP Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            
            // Koin Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // Multiplatform Settings for local persistence
            implementation(libs.multiplatform.settings)
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            
            // Coil 3 KMP for Image Loading
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")
            
            // ZXing QR library
            implementation("com.google.zxing:core:3.5.2")
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("com.google.android.material:material:1.11.0")
            
            // Ktor OkHttp Client for Android
            implementation(libs.ktor.client.okhttp)
            
            // Koin Android integration
            implementation(libs.koin.android)
            
            // Android-specific player libraries
            implementation("androidx.media3:media3-exoplayer:1.3.1")
            implementation("androidx.media3:media3-ui:1.3.1")
            implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
            
            // Install Referrer
            implementation("com.android.installreferrer:installreferrer:2.2")
            
            // Firebase
            implementation(dependencies.platform("com.google.firebase:firebase-bom:32.7.0"))
            implementation("com.google.firebase:firebase-messaging-ktx")
            implementation("com.google.firebase:firebase-analytics-ktx")
        }
        
        iosMain.dependencies {
            // Ktor Darwin Client for iOS
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.cinepass"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cinepass"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
}
