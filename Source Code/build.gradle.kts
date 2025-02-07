buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
    }
}

plugins {
    id("com.autonomousapps.dependency-analysis") version "1.18.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}