plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Compose compiler
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.canyoufix.pass"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.canyoufix.pass"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":data"))
    implementation(project(":crypto"))
    implementation(project(":autofill"))
    implementation(project(":sync"))

    // Compose
    implementation(libs.ui) // Для UI компонентов Compose
    implementation(libs.material3) // Для Material 3
    implementation(libs.androidx.foundation) // Для Column, Spacer и других
    implementation(libs.ui.tooling.preview) // Для Preview
    implementation(libs.androidx.navigation.compose) // Для навигации

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.android)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(project(":settings"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}