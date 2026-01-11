import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish)
    kotlin("native.cocoapods")
}

group = "com.jakebarnby.stripe"
version = "1.0.0"

// BUILD-03: Enable explicit API mode
kotlin {
    explicitApi()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    // BUILD-01: Add iosX64 target for Intel-based simulator
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "StripeKMP"
            isStatic = true
        }

        // Note: Swift bridge is provided as reference implementation in iosApp/
        // For production use, implement the bridge in your iOS app project
    }

    cocoapods {
        summary = "Kotlin Multiplatform wrapper for Stripe SDK"
        homepage = "https://github.com/jakebarnby/stripe-kmp"
        // MEDIUM-07: Document iOS SDK version
        ios.deploymentTarget = "13.0"
        framework {
            baseName = "StripeKMP"
            isStatic = true
        }

        // iOS Stripe SDK version: 24.5.0
        // Note: This should be kept in sync with Android version where possible
        pod("StripePaymentSheet") {
            version = "24.5.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("StripeFinancialConnections") {
            version = "24.5.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    // MEDIUM-08: Remove binaries.executable() for library module
    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.atomicfu)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.stripe.android)
            implementation(libs.stripe.financial.connections)
            implementation(libs.stripe.identity)
            implementation(libs.androidx.core.ktx)
        }

        iosMain.dependencies {
            // iOS uses CocoaPods for Stripe SDK
        }

        jsMain.dependencies {
            // Stripe.js npm package for module-based loading
            implementation(npm("@stripe/stripe-js", "2.4.0"))
        }

        wasmJsMain.dependencies {
            // WASM will use similar approach to JS
        }
    }
}

android {
    namespace = "com.jakebarnby.stripe"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        // BUILD-02: Add consumer ProGuard rules
        consumerProguardFiles("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
        // Disable RestrictedApi check - required for wrapper library accessing Stripe SDK internals
        disable += "RestrictedApi"
        // Treat warnings as errors in release builds
        abortOnError = true
        checkReleaseBuilds = true
    }
}


// Code coverage configuration
kover {
    reports {
        filters {
            excludes {
                // Exclude platform-specific implementations that require real SDK/Activity
                classes(
                    // Android implementations requiring Activity/Context
                    "com.jakebarnby.stripe.Stripe\$*",
                    "com.jakebarnby.stripe.PaymentSheet\$*",
                    "com.jakebarnby.stripe.PaymentAuthenticator\$*",
                    "com.jakebarnby.stripe.GooglePayLauncher\$*",
                    "com.jakebarnby.stripe.ApplePayLauncher\$*",
                    "com.jakebarnby.stripe.FinancialConnectionsSheet\$*",
                    "com.jakebarnby.stripe.IdentityVerificationSheet\$*",
                    // Android mappers (require real Stripe SDK types)
                    "com.jakebarnby.stripe.AndroidMappersKt*",
                    // Generated code
                    "*\$\$serializer",
                    "*\$Companion"
                )
            }
        }
        verify {
            rule {
                minBound(75) // Require 75% coverage on testable code (excludes platform implementations)
            }
        }
    }
}
