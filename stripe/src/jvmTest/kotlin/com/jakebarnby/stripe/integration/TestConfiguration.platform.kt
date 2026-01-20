package com.jakebarnby.stripe.integration

internal actual fun platformGetSystemProperty(name: String): String? = System.getProperty(name)

internal actual fun platformGetEnvironmentVariable(name: String): String? = System.getenv(name)

internal actual fun isJvmPlatform(): Boolean = true
