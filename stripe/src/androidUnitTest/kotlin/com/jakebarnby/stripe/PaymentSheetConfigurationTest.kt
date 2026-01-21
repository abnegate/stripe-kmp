package com.jakebarnby.stripe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaymentSheetConfigurationTest {

    @Test
    fun rejectsBlankMerchantDisplayName() {
        assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(merchantDisplayName = " ")
        }
    }

    @Test
    fun rejectsIncompleteCustomerConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Demo Shop",
                customerId = "cus_123",
                customerEphemeralKeySecret = null
            )
        }

        assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Demo Shop",
                customerId = null,
                customerEphemeralKeySecret = "eph_123"
            )
        }
    }

    @Test
    fun acceptsCustomerIdWithEphemeralKey() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Demo Shop",
            customerId = "cus_123",
            customerEphemeralKeySecret = "eph_123",
            allowsDelayedPaymentMethods = false
        )

        assertEquals("Demo Shop", config.merchantDisplayName)
        assertEquals("cus_123", config.customerId)
        assertEquals("eph_123", config.customerEphemeralKeySecret)
        assertEquals(false, config.allowsDelayedPaymentMethods)
    }

    @Test
    fun toStringRedactsEphemeralKey() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Demo Shop",
            customerId = "cus_123",
            customerEphemeralKeySecret = "eph_secret"
        )

        val text = config.toString()
        assertTrue(text.contains("***REDACTED***"))
        assertTrue(!text.contains("eph_secret"))
    }

    @Test
    fun paymentIntentConfigurationValidatesClientSecret() {
        val sheetConfig = PaymentSheetConfiguration(merchantDisplayName = "Demo Shop")

        assertFailsWith<IllegalArgumentException> {
            PaymentIntentConfiguration(
                clientSecret = "pi_invalid",
                paymentSheetConfiguration = sheetConfig
            )
        }

        val valid = PaymentIntentConfiguration(
            clientSecret = "pi_123_secret_456",
            paymentSheetConfiguration = sheetConfig
        )
        assertEquals("pi_123_secret_456", valid.clientSecret)
    }

    @Test
    fun setupIntentConfigurationValidatesClientSecret() {
        val sheetConfig = PaymentSheetConfiguration(merchantDisplayName = "Demo Shop")

        assertFailsWith<IllegalArgumentException> {
            SetupIntentConfiguration(
                clientSecret = "seti_invalid",
                paymentSheetConfiguration = sheetConfig
            )
        }

        val valid = SetupIntentConfiguration(
            clientSecret = "seti_123_secret_456",
            paymentSheetConfiguration = sheetConfig
        )
        assertEquals("seti_123_secret_456", valid.clientSecret)
    }
}
