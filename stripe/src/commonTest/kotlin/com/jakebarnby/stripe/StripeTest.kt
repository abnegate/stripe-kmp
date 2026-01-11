package com.jakebarnby.stripe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StripeTest {

    @Test
    fun testStripeConfiguration() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test Merchant",
            enableLogging = true
        )

        assertEquals("pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz", config.publishableKey)
        assertEquals("Test Merchant", config.merchantDisplayName)
        assertTrue(config.enableLogging)
    }

    @Test
    fun testStripeConfigurationDefaults() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Xyz987Wvu654Tsr321Pon098Mlk765Jih432Gfe210Dc",
            merchantDisplayName = "Default Merchant"
        )

        assertEquals("pk_test_51Xyz987Wvu654Tsr321Pon098Mlk765Jih432Gfe210Dc", config.publishableKey)
        assertEquals("Default Merchant", config.merchantDisplayName)
        assertFalse(config.enableLogging)
    }

    @Test
    fun testStripeConfigurationRejectsSecretKey() {
        val exception = assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "sk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
                merchantDisplayName = "Test Merchant"
            )
        }
        assertTrue(exception.message?.contains("Secret keys") == true)
    }

    @Test
    fun testStripeConfigurationRejectsInvalidPrefix() {
        val exception = assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "invalid_key_123",
                merchantDisplayName = "Test Merchant"
            )
        }
        assertTrue(exception.message?.contains("Invalid publishable key format") == true)
    }

    @Test
    fun testStripeConfigurationRejectsBlankMerchantName() {
        val exception = assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
                merchantDisplayName = ""
            )
        }
        assertTrue(exception.message?.contains("Merchant display name cannot be blank") == true)
    }

    @Test
    fun testPaymentSheetConfigurationRejectsBlankMerchantName() {
        val exception = assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = ""
            )
        }
        assertTrue(exception.message?.contains("Merchant display name cannot be blank") == true)
    }

    @Test
    fun testPaymentSheetConfigurationRejectsMismatchedCustomerConfig() {
        val exceptionOnlyCustomerId = assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Test Merchant",
                customerId = "cus_123",
                customerEphemeralKeySecret = null
            )
        }
        assertTrue(exceptionOnlyCustomerId.message?.contains("Both customerId and customerEphemeralKeySecret must be provided together") == true)

        val exceptionOnlyEphemeralKey = assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Test Merchant",
                customerId = null,
                customerEphemeralKeySecret = "ek_test_123"
            )
        }
        assertTrue(exceptionOnlyEphemeralKey.message?.contains("Both customerId and customerEphemeralKeySecret must be provided together") == true)
    }

    @Test
    fun testPaymentIntentConfigurationRejectsInvalidClientSecret() {
        val exception = assertFailsWith<IllegalArgumentException> {
            PaymentIntentConfiguration(
                clientSecret = "invalid_secret",
                paymentSheetConfiguration = PaymentSheetConfiguration(
                    merchantDisplayName = "Test Merchant"
                )
            )
        }
        assertTrue(exception.message?.contains("Invalid PaymentIntent client secret format") == true)
    }

    @Test
    fun testSetupIntentConfigurationRejectsInvalidClientSecret() {
        val exception = assertFailsWith<IllegalArgumentException> {
            SetupIntentConfiguration(
                clientSecret = "invalid_secret",
                paymentSheetConfiguration = PaymentSheetConfiguration(
                    merchantDisplayName = "Test Merchant"
                )
            )
        }
        assertTrue(exception.message?.contains("Invalid SetupIntent client secret format") == true)
    }

    // Platform-specific tests for Stripe initialization are in androidTest, iosTest, etc.
    // These tests only verify the configuration data classes work correctly
}
