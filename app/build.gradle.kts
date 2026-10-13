
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.fristfuchs.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.fristfuchs.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
