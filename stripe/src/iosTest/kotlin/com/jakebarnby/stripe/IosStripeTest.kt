package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * iOS-specific Stripe SDK tests.
 *
 * These tests verify the iOS implementation of the Stripe SDK,
 * which uses the shared StripeApiClient for headless operations
 * and the native StripePaymentSheet SDK for UI components.
 */
class IosStripeTest {


    @Test
    fun testStripeConfiguration_creation() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test iOS Merchant",
            enableLogging = true
        )

        assertEquals("pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz", config.publishableKey)
        assertEquals("Test iOS Merchant", config.merchantDisplayName)
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
    fun testCardParams_creationOnIos() {
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
    fun testPaymentMethodCreateParams_creationOnIos() {
        val params = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123",
            billingDetails = BillingDetails(name = "iOS User")
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertNotNull(params.billingDetails)
    }


    @Test
    fun testHttpClientEngine_creation() {
        // The iOS HttpClientEngine uses Darwin engine
        val engine = createHttpClientEngine()
        assertNotNull(engine)
    }


    @Test
    fun testIdempotencyKey_generationOnIos() {
        val key1 = IdempotencyKey.generate()
        val key2 = IdempotencyKey.generate()

        assertNotNull(key1.value)
        assertNotNull(key2.value)
        assertTrue(key1.value != key2.value, "Generated keys should be unique")
        assertEquals(32, key1.value.length)
    }


    @Test
    fun testStripeResult_successOnIos() {
        val result: StripeResult<String> = StripeResult.success("ios_test")
        assertTrue(result.isSuccess())
        assertEquals("ios_test", result.getOrNull())
    }

    @Test
    fun testStripeResult_failureOnIos() {
        val error = StripeException("iOS error")
        val result: StripeResult<String> = StripeResult.failure(error)
        assertTrue(result.isFailure())
        assertEquals("iOS error", result.errorOrNull()?.message)
    }


    @Test
    fun testSourceParams_bancontactOnIos() {
        val params = SourceParams.createBancontactParams(
            amount = 1000,
            name = "iOS Customer",
            returnUrl = "myapp://stripe-redirect"
        )

        assertEquals(SourceType.BANCONTACT, params.type)
        assertEquals(1000L, params.amount)
    }

    @Test
    fun testSourceParams_idealOnIos() {
        val params = SourceParams.createIdealParams(
            amount = 2000,
            name = "iOS Customer",
            returnUrl = "myapp://stripe-redirect",
            bank = "abn_amro"
        )

        assertEquals(SourceType.IDEAL, params.type)
    }


    @Test
    fun testConfirmPaymentIntentParams_onIos() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_ios_test_123",
            clientSecret = "pi_ios_secret_456",
            returnUrl = "myapp://stripe-redirect"
        )

        assertEquals("pm_ios_test_123", params.paymentMethodId)
        assertEquals("pi_ios_secret_456", params.clientSecret)
        assertEquals("myapp://stripe-redirect", params.returnUrl)
    }


    @Test
    fun testConfirmSetupIntentParams_onIos() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_ios_test_789",
            clientSecret = "seti_ios_secret_012"
        )

        assertEquals("pm_ios_test_789", params.paymentMethodId)
        assertEquals("seti_ios_secret_012", params.clientSecret)
    }
}
