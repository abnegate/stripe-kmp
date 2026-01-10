plugins {
    kotlin("multiplatform")
}

kotlin {
    // JVM target (can run on Android)
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "StripeKMP"
            isStatic = true
        }
    }
    
    // JS/Browser target
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.library()
    }
    
    // Wasm target
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.library()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Stripe Java SDK for actual integration
                implementation("com.stripe:stripe-java:28.0.0")
            }
        }
        
        val jsMain by getting {
            dependencies {
                // Stripe.js will be loaded via external script
            }
        }
        
        val wasmJsMain by getting {
            dependencies {
                // Stripe.js will be loaded via external script
            }
        }
    }
}
