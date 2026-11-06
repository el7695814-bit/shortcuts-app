plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // שנה את שני הערכים האלה לשם החבילה שלך לפני פרסום
    namespace = "com.example.linkshortcuts"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.linkshortcuts"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation("androidx.activity:activity-ktx:1.9.0")
}
