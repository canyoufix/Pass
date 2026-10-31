plugins {
    alias(libs.plugins.android.library)

    // Compose compiler
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.canyoufix.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":data"))
    implementation(project(":crypto"))
    implementation(project(":settings"))
    implementation(project(":sync"))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui) // Для UI компонентов Compose
    implementation(libs.androidx.material3) // Для Material 3
    implementation(libs.androidx.foundation) // Для Column, Spacer и других
    implementation(libs.androidx.ui.tooling.preview) // Для Preview
    implementation(libs.androidx.navigation.compose) // Для навигации
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation (libs.androidx.material.icons.extended)

    implementation(platform(libs.koin.bom))
    // Koin
    implementation (libs.koin.androidx.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
