package com.jakebarnby.stripe.integration

internal actual fun platformGetSystemProperty(name: String): String? = null

internal actual fun platformGetEnvironmentVariable(name: String): String? = null

internal actual fun isJvmPlatform(): Boolean = false
