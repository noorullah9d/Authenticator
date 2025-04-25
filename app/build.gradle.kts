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
        versionCode = 13
        versionName = "1.1.2"
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.vision)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    implementation(libs.firebase.auth)

    // coil
    implementation(libs.coil)

    // ump
    implementation(libs.user.messaging.platform)

    // admob
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // fb mediation
    implementation("com.google.ads.mediation:facebook:6.17.0.0")

    // mintegral mediation
    implementation("com.google.ads.mediation:mintegral:16.7.81.0")

    // applovin mediation
    implementation("com.google.ads.mediation:applovin:12.5.0.1")

    // liftOff mediation
    implementation("com.google.ads.mediation:vungle:7.4.0.0")

    //shimmer
    implementation(libs.shimmer)

    // in-app purchase
    implementation(libs.billing.ktx)

    // Google SignIn
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

    implementation(libs.firebase.bom)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.barcode.scanning)

    implementation(libs.release)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation("commons-codec:commons-codec:1.15")
    implementation("com.lambdapioneer.argon2kt:argon2kt:1.4.0")

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")

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