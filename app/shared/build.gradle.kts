import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // exposed via `api` so :app:androidApp can call App() without re-declaring Compose
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)

            // === OTEL TOGGLE: uncomment these 5 lines to break the iOS build on Kotlin 2.0 ===
            // (klib ABI 2.3.0 from the 2.3.21 compiler > the 2.0.0 toolchain ceiling 2.1.0)
            // See otel-break.log for the captured failure.
            // implementation(libs.otel.api)
            // implementation(libs.otel.sdkApi)
            // implementation(libs.otel.core)
            // implementation(libs.otel.implementation)
            // implementation(libs.otel.exportersCore)
        }
    }
}

android {
    namespace = "com.example.kmp.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
