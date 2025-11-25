plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Para poder utilizar Firebase Remote Config
    id("com.google.gms.google-services")

}



android {
    namespace = "miravalles.tumareapro"
    compileSdk = 35


    signingConfigs {
        create("configMareando") {
            storeFile = file("../nueva-miravalles-key.keystore")
            storePassword = project.properties["PASSWORD"] as String
            keyAlias = "miravalles"
            keyPassword = project.properties["PASSWORD"] as String
        }

        create("configTumarea") {
            storeFile = file("../tumarea-miravalles-key.keystore")
            storePassword = project.properties["PASSWORD"] as String
            keyAlias = "miravalles"
            keyPassword = project.properties["PASSWORD"] as String

        }
    }


    defaultConfig {
        applicationId = "miravalles.mareame"
        minSdk = 24
        targetSdk = 35
        versionCode = 41
        versionName = "2026-2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += listOf("version")
    productFlavors {
        create("tumarea") {
            dimension = "version"
            applicationId = "miravalles.tumarea"
            resValue("string", "app_name", "Tu Marea 2026")
            resValue("string", "flavor", "tumarea")
        }
        create("mareando") {
            dimension = "version"
            applicationId = "miravalles.mareame"
            resValue("string", "app_name", "Mareando")
            resValue("string", "flavor", "mareando")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            getByName("debug") {
                signingConfig = signingConfigs.getByName("debug")
            }

            productFlavors.getByName("mareando") {
                signingConfig = signingConfigs.getByName("configMareando")
            }

            productFlavors.getByName("tumarea") {
                signingConfig = signingConfigs.getByName("configTumarea")
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
}

dependencies {

    // Firebase
    implementation("com.google.firebase:firebase-config")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.viewpager:viewpager:1.1.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}