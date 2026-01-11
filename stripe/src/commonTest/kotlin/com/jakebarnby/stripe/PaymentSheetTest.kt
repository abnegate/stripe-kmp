package com.jakebarnby.stripe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentSheetTest {

    @Test
    fun testPaymentSheetConfigurationWithCustomerId() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Test Merchant",
            customerId = "cus_test123",
            customerEphemeralKeySecret = "ek_test_secret",
            allowsDelayedPaymentMethods = true
        )

        assertEquals("Test Merchant", config.merchantDisplayName)
        assertEquals("cus_test123", config.customerId)
        assertEquals("ek_test_secret", config.customerEphemeralKeySecret)
        assertTrue(config.allowsDelayedPaymentMethods)
    }

    @Test
    fun testPaymentSheetConfigurationDefaults() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Test Merchant"
        )

        assertEquals("Test Merchant", config.merchantDisplayName)
        assertEquals(null, config.customerId)
        assertEquals(null, config.customerEphemeralKeySecret)
        assertTrue(config.allowsDelayedPaymentMethods)
    }

    @Test
    fun testPaymentIntentConfigurationCreation() {
        // HIGH-05: Use valid PaymentIntent client secret format
        val config = PaymentIntentConfiguration(
            clientSecret = "pi_test123_secret_abc456",
            paymentSheetConfiguration = PaymentSheetConfiguration(
                merchantDisplayName = "Intent Test"
            )
        )

        assertEquals("pi_test123_secret_abc456", config.clientSecret)
        assertNotNull(config.paymentSheetConfiguration)
    }

    @Test
    fun testSetupIntentConfigurationCreation() {
        // HIGH-05: Use valid SetupIntent client secret format
        val config = SetupIntentConfiguration(
            clientSecret = "seti_test456_secret_xyz789",
            paymentSheetConfiguration = PaymentSheetConfiguration(
                merchantDisplayName = "Setup Test"
            )
        )

        assertEquals("seti_test456_secret_xyz789", config.clientSecret)
        assertNotNull(config.paymentSheetConfiguration)
    }

    @Test
    fun testPaymentSheetResultSuccess() {
        val result = PaymentSheetResult.Completed
        assertTrue(result is PaymentSheetResult.Completed)
    }

    @Test
    fun testPaymentSheetResultCanceled() {
        val result = PaymentSheetResult.Canceled
        assertTrue(result is PaymentSheetResult.Canceled)
    }

    @Test
    fun testPaymentSheetResultFailed() {
        val error = StripeError("Test error", "test_code")
        val result = PaymentSheetResult.Failed(error)

        assertTrue(result is PaymentSheetResult.Failed)
        assertEquals("Test error", result.error.message)
        assertEquals("test_code", result.error.code)
    }
}
