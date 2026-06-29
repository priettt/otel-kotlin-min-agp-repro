buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    // declared here so the plugin classes load once and are shared across subprojects
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
