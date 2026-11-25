import java.util.Properties
import java.net.URI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}
// Lee la URL personalizada sin reemplazar la configuración del SDK.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input -> localProperties.load(input) }
}
// Usa la dirección del emulador si no se configuró otra en local.properties.
val oylApiBaseUrl = (providers.gradleProperty("oylApiBaseUrl").orNull
    ?: localProperties.getProperty("oylApiBaseUrl", "http://10.0.2.2:8080/api/")).trim().trimEnd('/') + "/"
val gatewayUri = URI.create(oylApiBaseUrl)
require(gatewayUri.scheme in listOf("http", "https") && gatewayUri.host != null && gatewayUri.path == "/api/" &&
        gatewayUri.userInfo == null && gatewayUri.query == null && gatewayUri.fragment == null) {
    "oylApiBaseUrl debe tener la forma http://IP:8080/api/"
}

android {
    namespace = "cl.oyl.mobile"
    compileSdk = 35
    defaultConfig {
        applicationId = "cl.oyl.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 15
        versionName = "4.1.2"
        buildConfigField("String", "OYL_API_BASE_URL", "\"$oylApiBaseUrl\"") // Expone la URL a Retrofit.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (System.getenv("OYL_SIGN_STORE") != null) create("aulaKey") {
            storeFile = file(System.getenv("OYL_SIGN_STORE"))
            storePassword = System.getenv("OYL_SIGN_PASSWORD")
            keyAlias = "aula"
            keyPassword = System.getenv("OYL_SIGN_PASSWORD")
        }
    }
    buildTypes {
        getByName("debug") { enableUnitTestCoverage = true }
        release { isMinifyEnabled = false }
        create("aula") {
            initWith(getByName("release"))
            applicationIdSuffix = ".aula"
            versionNameSuffix = "-aula"
            signingConfig = signingConfigs.findByName("aulaKey")
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testCoverage { jacocoVersion = "0.8.12" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    kapt("androidx.room:room-compiler:2.8.5")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
