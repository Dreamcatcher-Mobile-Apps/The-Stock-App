android {
    compileSdk = 33
    defaultConfig {
        applicationId = "dreamcatcher.thestockapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

dependencies {

    implementation("android.arch.lifecycle:extensions:1.1.1")
    kapt("android.arch.lifecycle:compiler:1.1.1")

    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("androidx.activity:activity-ktx:1.6.1")

    implementation("com.google.dagger:dagger:2.27")
    kapt("com.google.dagger:dagger-compiler:2.27")
    compileOnly("org.glassfish:javax.annotation:3.1.1")

    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.42")

    implementation("androidx.room:room-runtime:2.2.5")
    kapt("androidx.room:room-compiler:2.2.5")

    testImplementation("org.mockito:mockito-inline:2.13.0")
    testImplementation("android.arch.core:core-testing:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")

    implementation("com.google.code.gson:gson:2.8.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.4")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    implementation("com.google.android.material:material:1.8.0")
}