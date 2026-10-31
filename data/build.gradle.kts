plugins {
    alias(libs.plugins.android.library)

    // KSP
    alias(libs.plugins.ksp)

    // JSON
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.canyoufix.data"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":crypto"))
    implementation(project(":settings"))
    implementation(project(":sync"))
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.androidx.work.runtime.ktx)

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // JSON
    implementation(libs.kotlinx.serialization.json)

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
