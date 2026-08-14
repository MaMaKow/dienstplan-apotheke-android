plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.mamakow.dienstplanapotheke"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.mamakow.dienstplanapotheke"
        minSdk = 26
        targetSdk = 37
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

// Konfiguration, um Tests bei jedem Build auszuführen
tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Macht assemble-Tasks von den Tests abhängig
tasks.matching { it.name.startsWith("assemble") }.configureEach {
    dependsOn(tasks.matching { it.name.startsWith("test") && it.name.endsWith("UnitTest") })
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.swiperefreshlayout)
    implementation(libs.preference.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric)
    testImplementation(libs.arch.core.testing)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockwebserver)

    implementation(libs.dotenv)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    annotationProcessor(libs.room.compiler)
}
