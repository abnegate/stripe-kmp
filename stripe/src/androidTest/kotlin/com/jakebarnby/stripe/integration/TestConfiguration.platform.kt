package com.jakebarnby.stripe.integration

// Note: These functions shadow the expect/actual declarations in commonTest.
// androidInstrumentedTest cannot depend on commonTest due to source set tree restrictions,
// so we provide standalone implementations here that mirror the JVM behavior.
internal fun platformGetSystemProperty(name: String): String? = System.getProperty(name)

internal fun platformGetEnvironmentVariable(name: String): String? = System.getenv(name)

internal fun isJvmPlatform(): Boolean = false
