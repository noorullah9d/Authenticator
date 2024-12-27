plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.my.project.authenticator"
    compileSdk = 34


    signingConfigs {
        create("release") {
            keyAlias = "galixoai"
            keyPassword = "galixoai"
            storeFile = file("C:/Users/HP/Desktop/authenticator.jks")
            storePassword = "galixoai"
        }
    }


    defaultConfig {
        applicationId = "com.authenticator.manager.password.generator"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.2"
        setProperty("archivesBaseName", "MF_Authenticator_App" + "_vc_" + versionCode + "_vn_" + versionName + "_")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APPLICATION_ID", "\"$applicationId\"")
    }

    bundle {
        language {
            enableSplit = false
        }
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig signingConfigs.release
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
//            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.vision)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    implementation(libs.firebase.bom)

    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.analytics)
    implementation("com.google.firebase:firebase-crashlytics:19.1.0")


    implementation(libs.barcode.scanning)



    implementation(libs.release)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)


    implementation("commons-codec:commons-codec:1.15")
    implementation("com.lambdapioneer.argon2kt:argon2kt:1.4.0")


    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    //lottie animation
    implementation("com.airbnb.android:lottie:6.0.1")

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    //serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.github.wdsqjq:AndRatingBar:1.0.6")

    implementation("com.github.rahulabrol:Android-Fingerprint:1.0.4")

}

