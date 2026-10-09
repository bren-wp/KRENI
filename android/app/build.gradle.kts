plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingStore = System.getenv("KRENI_SIGNING_STORE")
val signingPassword = System.getenv("KRENI_SIGNING_PASSWORD")
val signingAlias = System.getenv("KRENI_SIGNING_ALIAS")
val signingKeyPassword = System.getenv("KRENI_SIGNING_KEY_PASSWORD")
val hasProductionSigning = listOf(signingStore, signingPassword, signingAlias, signingKeyPassword)
    .all { !it.isNullOrBlank() }

android {
    signingConfigs {
        if (hasProductionSigning) {
            create("production") {
                storeFile = file(requireNotNull(signingStore))
                storePassword = requireNotNull(signingPassword)
                keyAlias = requireNotNull(signingAlias)
                keyPassword = requireNotNull(signingKeyPassword)
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }
    namespace = "hr.brendigo.kreni"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.brendigo.kreni"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.0.4"
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    sourceSets.getByName("main").assets.srcDir(rootProject.file("../data"))

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasProductionSigning) {
                signingConfig = signingConfigs.getByName("production")
            }
        }
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("org.maplibre.gl:android-sdk:11.8.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
}
