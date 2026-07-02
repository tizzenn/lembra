plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lembra.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lembra.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.6"
    }

    // La clave de firma llega por variables de entorno (en CI, desde los secrets del repo).
    signingConfigs {
        create("release") {
            val rutaKeystore = System.getenv("LEMBRA_KEYSTORE")
            if (rutaKeystore != null) {
                storeFile = file(rutaKeystore)
                storeType = "PKCS12"
                storePassword = System.getenv("LEMBRA_KEYSTORE_PASS")
                keyAlias = System.getenv("LEMBRA_KEY_ALIAS") ?: "lembra"
                keyPassword = System.getenv("LEMBRA_KEY_PASS") ?: System.getenv("LEMBRA_KEYSTORE_PASS")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (System.getenv("LEMBRA_KEYSTORE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        viewBinding = true
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Room (base de datos local)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
