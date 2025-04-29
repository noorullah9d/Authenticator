plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.my.project.authenticator"
    compileSdk = 34

    signingConfigs {
        create("release") {
            keyAlias = "galixoai"
            keyPassword = "galixoai"
//            storeFile = file("/Users/galixo/Desktop/Authenticator Keystore/authenticator.jks")
            storeFile = file("D:\\Authenticator Credientials/authenticator.jks")
            storePassword = "galixoai"
        }
    }

    defaultConfig {
        applicationId = "com.authenticator.manager.password.generator"
        minSdk = 24
        targetSdk = 34
        versionCode = 14
        versionName = "1.1.3"
        setProperty("archivesBaseName", "authenticator_v$versionCode($versionName)")
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

            // original ad ids
            resValue("string", "admob_app_id", "ca-app-pub-9555621625220566~7344809990")
            resValue("string", "admob_interstitial_id_splash", "ca-app-pub-9555621625220566/2331693442")
            resValue("string", "admob_interstitial_fragment", "ca-app-pub-9555621625220566/1060680660")
            resValue("string", "admob_interstitial_onboarding", "ca-app-pub-9555621625220566/1144384649")
            resValue("string", "admob_banner_id", "ca-app-pub-9555621625220566/1675758339")
            resValue("string", "admob_native_id_languages", "ca-app-pub-9555621625220566/6328478893")
            resValue("string", "admob_native_id_onboarding", "ca-app-pub-9555621625220566/2541283298")
            resValue("string", "admob_native_id_home", "ca-app-pub-9555621625220566/7452158186")
            resValue("string", "admob_native_id_qr", "ca-app-pub-9555621625220566/5211678661")
            resValue("string", "admob_native_id_backup_theme", "ca-app-pub-9555621625220566/1036629933")
            resValue("string", "admob_native_id_exit", "ca-app-pub-9555621625220566/3488862446")
            resValue("string", "admob_native_id_transfer_codes", "ca-app-pub-9555621625220566/7215625517")
            resValue("string", "admob_app_open_id", "ca-app-pub-9555621625220566/2140121757")
        }
        debug {
            // test ad ids
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_interstitial_id_splash", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_interstitial_fragment", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_interstitial_onboarding", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_banner_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_native_id_languages", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_onboarding", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_home", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_qr", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_backup_theme", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_exit", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_id_transfer_codes", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            isMinifyEnabled = false
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.vision)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.commons.codec)
    implementation(libs.argon2kt)

    // sdp
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)

    // coil
    implementation(libs.coil)

    // ump
    implementation(libs.user.messaging.platform)

    // admob
    implementation(libs.play.services.ads)

    // fb mediation
    implementation(libs.facebook)

    // mintegral mediation
    implementation(libs.mintegral)

    // applovin mediation
    implementation(libs.applovin)

    // liftOff mediation
    implementation(libs.vungle)

    //shimmer
    implementation(libs.shimmer)

    // in-app purchase
    implementation(libs.billing.ktx)

    // google sign in
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // firebase
    implementation(libs.firebase.bom)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.config)

    // gson
    implementation(libs.gson)

    // barcode scanner
    implementation(libs.barcode.scanning)

    // progress view
    implementation(libs.progress.view)

    // hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // lifecycle
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    //lottie
    implementation(libs.lottie)

    // cameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    //serialization
    implementation(libs.kotlinx.serialization.json)

    // rating bar
    implementation(libs.andratingbar)

    // fingerprint
    implementation(libs.android.fingerprint)
}