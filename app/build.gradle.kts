import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
}
// קריאת המפתח מ‑local.properties
val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
val apiKey = localProps.getProperty("GOOGLE_API_KEY") ?: ""
android {
    namespace = "com.example.minesweeper"
    compileSdk = 36
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            // מומלץ גם להחריג קבצי מטא נפוצים נוספים (למקרה שיופיעו):
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/LICENSE*"
        }
    }
    defaultConfig {
        applicationId = "com.example.minesweeper"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // חשיפת המפתח לקוד Java כ‑BuildConfig.GOOGLE_API_KEY
        buildConfigField("String", "GOOGLE_API_KEY", "\"$apiKey\"")
        // חלופה (ללא BuildConfig):
        // resValue("string", "google_api_key", apiKey)
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
        buildFeatures {
            buildConfig = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.google.genai) // SDK רשמי ל‑Gemini
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}