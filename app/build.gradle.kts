plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Obtainium release signing uses a permanent, owner-controlled keystore from CI secrets.
val releaseKeystorePath = providers.environmentVariable("FEEDVEIL_KEYSTORE_FILE").orNull
val releaseStorePassword = providers.environmentVariable("FEEDVEIL_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("FEEDVEIL_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("FEEDVEIL_KEY_PASSWORD").orNull
val releaseSigningReady = listOf(
    releaseKeystorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.abdulrahmanalsaeed.feedveil"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.abdulrahmanalsaeed.feedveil"
        minSdk = 29
        targetSdk = 36
        versionCode = providers.gradleProperty("feedveilVersionCode").orNull?.toIntOrNull() ?: 3
        versionName = providers.gradleProperty("feedveilVersionName").orNull
            ?: "0.1.0-phase1-preview"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningReady) {
            create("feedveilRelease") {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseSigningReady) {
                signingConfig = signingConfigs.getByName("feedveilRelease")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.16.0")

    testImplementation("junit:junit:4.13.2")
}
