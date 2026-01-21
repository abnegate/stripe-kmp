package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.*

class GooglePayLauncherTest {

    @Test
    fun testGooglePayLauncher_instantiation() {
        val launcher = GooglePayLauncher()
        assertNotNull(launcher)
    }

    @Test
    fun testGooglePayLauncher_isAvailable_returnsBoolean() {
        // Note: This will vary by platform
        val available = GooglePayLauncher.isAvailable()
        // Just verify it returns a boolean without throwing
        assertTrue(available is Boolean)
    }

    @Test
    fun testGooglePayLauncher_isAvailable_withNullContext() {
        // Should not throw
        val available = GooglePayLauncher.isAvailable(null)
        assertTrue(available is Boolean)
    }

    // Platform-specific tests would be in platform-specific test directories
}
