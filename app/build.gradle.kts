plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.safeArgs)
    kotlin("kapt")
    kotlin("plugin.parcelize")
    alias(libs.plugins.dagger.hilt)

}

android {
    namespace = "com.example.mobilemhealthpay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mobilemhealthpay"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true

    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.room)
    implementation(libs.androidx.glide)
    implementation(libs.androidx.hilt.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.viewpager2)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.timber)
    implementation(libs.pdf)
    implementation(libs.easy.permission)
    implementation(libs.badge)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.journeyapps)
    implementation(libs.play.services.location)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.gson.converter)
    implementation (libs.eventbus)
    //implementation(libs.ussd.library)
    ksp(libs.androidx.room.compiler)
    kapt(libs.hilt.android.compiler)
    ksp(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.scalars)
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
}