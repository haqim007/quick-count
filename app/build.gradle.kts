plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.navigation.safeargs.kotlin)
    alias(libs.plugins.gms.google.services)
}

android {
    namespace = "com.haltec.quickcount"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.haltec.quickcount"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("BASE_URL", "http://quickcount-api.haltec.id:81/v1/")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            buildConfigField("BASE_URL", "https://quickcount-api.haltec.id:81/v1/")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.paging.runtime.ktx)
    implementation(libs.glide)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.paging.runtime.ktx)
    implementation(libs.room.paging)
    implementation(libs.airbnb.lottie)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.github.mikeortiz.touchimageview)
    implementation(libs.gms.playservices.maps)
    implementation(libs.gms.playservices.location)
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))
}

inline fun <reified ValueT> com.android.build.api.dsl.VariantDimension.buildConfigField(
    name: String,
    value: ValueT
) {
    val resolvedValue = when (value) {
        is String -> "\"$value\""
        else -> value
    }.toString()
    buildConfigField(ValueT::class.java.simpleName, name, resolvedValue)
}