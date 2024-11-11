plugins {
    alias(libs.plugins.android.application)
    //id("com.android.application") duplicated either keep alias or keep main id
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.easypark"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easypark"
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
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-database")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.15.0")
    implementation("com.github.bumptech.glide:annotations:4.15.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")
}