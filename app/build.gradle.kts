plugins {
    //alias(libs.plugins.android.application)
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.0.0"

}

android {
    namespace = "de.mamakow.dienstplanapotheke"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.mamakow.dienstplanapotheke"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.okhttp)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //implementation("io.github.cdimascio:dotenv-java:3.0.0")

    implementation(libs.dotenv)
    // Retrofit
    implementation(libs.retrofit)

    // Gson Converter für Retrofit
    implementation(libs.converter.gson)
    annotationProcessor(libs.room.compiler)
}
