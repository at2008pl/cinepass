plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.devtools.ksp") version "2.3.9" apply false

    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
