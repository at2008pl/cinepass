@file:Suppress("DEPRECATION")
@file:OptIn(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            disableNativeCache(
                version = DisableCacheInKotlinVersion.`2_3_20`,
                reason = "Work around navigation-runtime klib cache build failure on Kotlin 2.3.20",
            )
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material)
            api(compose.material3)
            api(compose.ui)
            api(compose.materialIconsExtended)
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)
            
            // Lifecycle Viewmodel & Runtime
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0-beta01")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0-beta01")
            
            // Navigation
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
            implementation("org.jetbrains.androidx.savedstate:savedstate:1.4.0")
            
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
        }
        
        androidMain.dependencies {
            // ZXing QR library (JVM/Android only)
            implementation("com.google.zxing:core:3.5.2")
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
    namespace = "com.cinepass.shared"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
