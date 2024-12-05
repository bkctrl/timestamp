import com.android.build.api.dsl.Packaging

group = "org.timestamp"
version = "2.0.0"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)

    // Add the Google services Gradle plugin
    alias(libs.plugins.google.services)
}

android {
    namespace = "org.timestamp.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.timestamp.mobile"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    packaging {
        resources{
            excludes.add("/META-INF/*")
            excludes.add("notice.txt")
            excludes.add("license.txt")
            excludes.add("META-INF/spring/*")
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.ui)
    implementation(libs.coil.compose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity.compose.v161)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps.v1810)
    implementation(libs.accompanist.permissions)
    implementation(libs.places)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.material3.android)
    implementation(libs.volley)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.googleid)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.composecalendar)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.play.services.location)
    testImplementation(libs.mockk.v1137)
    implementation(project(":lib"))
    implementation("com.github.vsnappy1:ComposeDatePicker:2.2.0") {
        exclude(group = "androidx.compose.material3", module = "material3")
    }
    implementation(libs.androidx.compose.material)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
}
