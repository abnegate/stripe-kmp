package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Android-specific Stripe SDK tests.
 *
 * These tests verify the Android implementation of the Stripe SDK,
 * which uses the native Stripe Android SDK for all operations.
 */
class AndroidStripeTest {


    @Test
    fun testStripeConfiguration_creation() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test Android Merchant",
            enableLogging = true
        )

        assertEquals("pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz", config.publishableKey)
        assertEquals("Test Android Merchant", config.merchantDisplayName)
        assertTrue(config.enableLogging)
    }

    @Test
    fun testStripeConfiguration_rejectsSecretKey() {
        assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "sk_test_secret_key",
                merchantDisplayName = "Test"
            )
        }
    }

    @Test
    fun testStripeConfiguration_rejectsBlankMerchantName() {
        assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
                merchantDisplayName = ""
            )
        }
    }

    @Test
    fun testStripeConfiguration_acceptsLiveKey() {
        val config = StripeConfiguration(
            publishableKey = "pk_live_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Live Merchant"
        )

        assertTrue(config.publishableKey.startsWith("pk_live_"))
    }


    @Test
    fun testCardParams_creationOnAndroid() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
    }

    @Test
    fun testCardParams_builderOnAndroid() {
        val params = CardParams.builder()
            .number("5555555555554444")
            .expMonth(6)
            .expYear(2026)
            .cvc("456")
            .name("Android User")
            .build()

        assertEquals("5555555555554444", params.number)
        assertEquals("Android User", params.name)
    }

    @Test
    fun testPaymentMethodCreateParams_creationOnAndroid() {
        val params = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123",
            billingDetails = BillingDetails(name = "Android User")
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertNotNull(params.billingDetails)
    }


    @Test
    fun testIdempotencyKey_generationOnAndroid() {
        val key1 = IdempotencyKey.generate()
        val key2 = IdempotencyKey.generate()

        assertNotNull(key1.value)
        assertNotNull(key2.value)
        assertTrue(key1.value != key2.value, "Generated keys should be unique")
        assertEquals(32, key1.value.length)
    }

    @Test
    fun testIdempotencyKey_customValue() {
        val key = IdempotencyKey.fromValue("android-custom-key-123")
        assertEquals("android-custom-key-123", key.value)
    }


    @Test
    fun testStripeResult_successOnAndroid() {
        val result: StripeResult<String> = StripeResult.success("android_test")
        assertTrue(result.isSuccess())
        assertEquals("android_test", result.getOrNull())
    }

    @Test
    fun testStripeResult_failureOnAndroid() {
        val error = StripeException("Android error")
        val result: StripeResult<String> = StripeResult.failure(error)
        assertTrue(result.isFailure())
        assertEquals("Android error", result.errorOrNull()?.message)
    }

    @Test
    fun testStripeResult_mapOnAndroid() {
        val result: StripeResult<Int> = StripeResult.success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, mapped.getOrNull())
    }


    @Test
    fun testSourceParams_cardOnAndroid() {
        val cardParams = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        val params = SourceParams.createCardParams(cardParams)

        assertEquals(SourceType.CARD, params.type)
    }

    @Test
    fun testSourceParams_bancontactOnAndroid() {
        val params = SourceParams.createBancontactParams(
            amount = 1000,
            name = "Android Customer",
            returnUrl = "myandroidapp://stripe-redirect"
        )

        assertEquals(SourceType.BANCONTACT, params.type)
        assertEquals(1000L, params.amount)
    }


    @Test
    fun testConfirmPaymentIntentParams_onAndroid() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_android_test_123",
            clientSecret = "pi_android_secret_456",
            returnUrl = "myandroidapp://stripe-redirect"
        )

        assertEquals("pm_android_test_123", params.paymentMethodId)
        assertEquals("pi_android_secret_456", params.clientSecret)
        assertEquals("myandroidapp://stripe-redirect", params.returnUrl)
    }

    @Test
    fun testConfirmPaymentIntentParams_withShippingOnAndroid() {
        val shipping = ShippingDetails(
            name = "Android User",
            address = Address(
                line1 = "123 Android Ave",
                city = "Mountain View",
                state = "CA",
                postalCode = "94043",
                country = "US"
            )
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_android_test_789",
            clientSecret = "pi_android_secret_012",
            shipping = shipping
        )

        assertNotNull(params.shipping)
        assertEquals("Android User", params.shipping?.name)
    }


    @Test
    fun testConfirmSetupIntentParams_onAndroid() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_android_test_345",
            clientSecret = "seti_android_secret_678"
        )

        assertEquals("pm_android_test_345", params.paymentMethodId)
        assertEquals("seti_android_secret_678", params.clientSecret)
    }


    @Test
    fun testGooglePayEnvironment_values() {
        // Test that GooglePayEnvironment enum values are accessible
        assertEquals("TEST", GooglePayEnvironment.TEST.name)
        assertEquals("PRODUCTION", GooglePayEnvironment.PRODUCTION.name)
    }

    @Test
    fun testGooglePayConfiguration_creation() {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Test Android Merchant",
            merchantCountryCode = "US"
        )

        assertEquals(GooglePayEnvironment.TEST, config.environment)
        assertEquals("Test Android Merchant", config.merchantName)
        assertEquals("US", config.merchantCountryCode)
    }

    @Test
    fun testGooglePayConfiguration_withAllowedCardNetworks() {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.PRODUCTION,
            merchantName = "Prod Merchant",
            merchantCountryCode = "US",
            allowedCardNetworks = listOf(CardBrand.VISA, CardBrand.MASTERCARD, CardBrand.AMERICAN_EXPRESS)
        )

        assertTrue(config.allowedCardNetworks.contains(CardBrand.VISA))
        assertTrue(config.allowedCardNetworks.contains(CardBrand.MASTERCARD))
    }


    @Test
    fun testPaymentSheetConfiguration_creation() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Android Merchant"
        )

        assertEquals("Android Merchant", config.merchantDisplayName)
    }

    @Test
    fun testPaymentSheetConfiguration_withCustomer() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Android Merchant",
            customerId = "cus_android_123",
            customerEphemeralKeySecret = "ek_android_456"
        )

        assertEquals("cus_android_123", config.customerId)
        assertNotNull(config.customerEphemeralKeySecret)
    }

    @Test
    fun testPaymentSheetConfiguration_rejectsMismatchedCustomerConfig() {
        // Only customerId without ephemeral key should fail
        assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Test",
                customerId = "cus_123",
                customerEphemeralKeySecret = null
            )
        }

        // Only ephemeral key without customerId should fail
        assertFailsWith<IllegalArgumentException> {
            PaymentSheetConfiguration(
                merchantDisplayName = "Test",
                customerId = null,
                customerEphemeralKeySecret = "ek_123"
            )
        }
    }


    @Test
    fun testPaymentIntentConfiguration_creation() {
        val paymentSheetConfig = PaymentSheetConfiguration(
            merchantDisplayName = "Android Test Merchant"
        )

        val config = PaymentIntentConfiguration(
            clientSecret = "pi_android_test_secret_123",
            paymentSheetConfiguration = paymentSheetConfig
        )

        assertEquals("pi_android_test_secret_123", config.clientSecret)
        assertNotNull(config.paymentSheetConfiguration)
    }

    @Test
    fun testPaymentIntentConfiguration_rejectsInvalidClientSecret() {
        assertFailsWith<IllegalArgumentException> {
            PaymentIntentConfiguration(
                clientSecret = "invalid_secret",
                paymentSheetConfiguration = PaymentSheetConfiguration(
                    merchantDisplayName = "Test"
                )
            )
        }
    }


    @Test
    fun testSetupIntentConfiguration_creation() {
        val paymentSheetConfig = PaymentSheetConfiguration(
            merchantDisplayName = "Android Test Merchant"
        )

        val config = SetupIntentConfiguration(
            clientSecret = "seti_android_test_secret_456",
            paymentSheetConfiguration = paymentSheetConfig
        )

        assertEquals("seti_android_test_secret_456", config.clientSecret)
        assertNotNull(config.paymentSheetConfiguration)
    }
}
