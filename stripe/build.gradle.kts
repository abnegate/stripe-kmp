import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinSerialization)
    kotlin("native.cocoapods")
}

group = "com.jakebarnby.stripe"
version = "1.0.0"

// BUILD-03: Enable explicit API mode
kotlin {
    applyDefaultHierarchyTemplate()

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
    }

    // XCFramework for SPM distribution
    val xcfTask = tasks.register("assembleXCFramework") {
        dependsOn("linkReleaseFrameworkIosArm64", "linkReleaseFrameworkIosSimulatorArm64", "linkReleaseFrameworkIosX64")

        doLast {
            val outputDir = layout.buildDirectory.dir("XCFramework").get().asFile
            outputDir.deleteRecursively()

            exec {
                commandLine(
                    "xcodebuild", "-create-xcframework",
                    "-framework", layout.buildDirectory.file("bin/iosArm64/releaseFramework/StripeKMP.framework").get().asFile.absolutePath,
                    "-framework", layout.buildDirectory.file("bin/iosSimulatorArm64/releaseFramework/StripeKMP.framework").get().asFile.absolutePath,
                    "-framework", layout.buildDirectory.file("bin/iosX64/releaseFramework/StripeKMP.framework").get().asFile.absolutePath,
                    "-output", outputDir.resolve("StripeKMP.xcframework").absolutePath
                )
            }
        }
    }

    cocoapods {
        summary = "Kotlin Multiplatform wrapper for Stripe SDK"
        homepage = "https://github.com/jakebarnby/stripe-kmp"
        ios.deploymentTarget = "13.0"
        framework {
            baseName = "StripeKMP"
            isStatic = true
        }

        // Stripe iOS SDK for PaymentSheet UI
        // Headless operations use REST API via Ktor
        pod("StripePaymentSheet") {
            version = "25.5.0"
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

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.atomicfu)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.robolectric)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.testJunit)
                implementation(libs.androidx.testExt.junit)
                implementation(libs.androidx.espresso.core)
            }
        }

        val clientMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                // Common Ktor dependencies for REST API
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        val applePayMain by creating {
            dependsOn(clientMain)
        }

        val googlePayMain by creating {
            dependsOn(clientMain)
        }

        androidMain {
            dependsOn(clientMain)
            dependsOn(googlePayMain)
            dependencies {
                // Ktor engine for Android
                implementation(libs.ktor.client.okhttp)
                // Native SDK for UI components only
                api(libs.stripe.android)
                api(libs.stripe.financial.connections)
                api(libs.stripe.identity)
                implementation(libs.androidx.core.ktx)
            }
        }

        val iosMain by getting {
            dependsOn(clientMain)
            dependsOn(applePayMain)
            dependencies {
                // Ktor engine for iOS
                implementation(libs.ktor.client.darwin)
            }
        }

        jsMain {
            dependsOn(clientMain)
            dependsOn(applePayMain)
            dependsOn(googlePayMain)
            dependencies {
                // Ktor engine for JS
                implementation(libs.ktor.client.js)
                // Stripe.js for UI components
                implementation(npm("@stripe/stripe-js", "5.5.0"))
            }
        }

        wasmJsMain {
            dependsOn(clientMain)
            dependsOn(applePayMain)
            dependsOn(googlePayMain)
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        jvmMain {
            dependsOn(clientMain)
            dependencies {
                api(libs.stripe.java)
                implementation(libs.ktor.client.cio)
            }
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
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
                classes(
                    "com.jakebarnby.stripe.*\$*\$*", // Nested lambdas/callbacks from Android UI code
                    "com.jakebarnby.stripe.GooglePayLauncher",
                    "com.jakebarnby.stripe.GooglePayLauncher\$*",
                    "com.jakebarnby.stripe.*_androidKt", // Android extension functions
                )
                // Android mappers - require real Stripe Android SDK types
                classes("com.jakebarnby.stripe.AndroidMappersKt")
                // JVM server operations - require real Stripe secret keys
                classes(
                    "com.jakebarnby.stripe.Stripe\$createAccountToken\$*",
                    "com.jakebarnby.stripe.Stripe\$createEphemeralKey\$*",
                    "com.jakebarnby.stripe.Stripe\$retrieveCustomer\$*",
                    "com.jakebarnby.stripe.Stripe_jvmKt",
                )
            }
        }
        verify {
            rule {
                minBound(75)
            }
        }
    }
}
