import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.fazli.vispar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fazli.vispar"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true // Add this line to enable BuildConfig generation
    }

    // removed signingConfigs so release won't be signed

    buildTypes {
        release {
            isMinifyEnabled = false
            // <-- removed signingConfig here so release stays unsigned
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // produce a minimal unsigned APK (no obfuscation/minify)
        }
    }

    // produce a single simple APK (disable per-ABI splits)
    splits {
        abi {
            isEnable = false
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Add support for different screen sizes including TV
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src/main/res", "src/main/res/values-television")
            }
        }
    }
    
    // Lint configuration to handle missing default resource issue
    lint {
        // Use baseline to ignore existing lint errors
        baseline = file("lint-baseline.xml")
        // Continue build even if lint errors are found
        abortOnError = false
        checkReleaseBuilds = false
    }

    // Optional: make output filename predictable/simple (may depend on AGP version)
    // If this block causes issues with your AGP version, حذفش کن — خروجی هنوز unsigned خواهد بود.
    applicationVariants.all {
        outputs.all {
            // set a simple, consistent filename for the release variant
            if (name.contains("release", ignoreCase = true)) {
                // outputFileName may be different depending on AGP; for many AGP versions this works
                setProperty("outputFileName", "app-release-unsigned.apk")
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.coil.compose)
    
    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    
    // Leanback for TV support
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.leanback.preference)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
