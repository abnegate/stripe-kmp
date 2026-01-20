package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.*
import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

/**
 * Integration tests for the Stripe API client.
 *
 * These tests validate the end-to-end behavior of the Stripe API client,
 * including request building, error handling, and response parsing.
 *
 * Note: Tests that require real API calls will be skipped if no API key
 * is configured. Model and validation tests always run.
 */
class ApiClientIntegrationTest : IntegrationTestBase() {


    @Test
    fun testConfiguration_rejectsSecretKey() {
        assertFailsWith<IllegalArgumentException> {
            StripeConfiguration(
                publishableKey = "sk_test_51Abc123",
                merchantDisplayName = "Test"
            )
        }
    }

    @Test
    fun testConfiguration_acceptsTestKey() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test Merchant"
        )
        assertTrue(config.publishableKey.startsWith("pk_test_"))
    }

    @Test
    fun testConfiguration_acceptsLiveKey() {
        val config = StripeConfiguration(
            publishableKey = "pk_live_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Live Merchant"
        )
        assertTrue(config.publishableKey.startsWith("pk_live_"))
    }


    @Test
    fun testCardParams_validation_acceptsValidCard() {
        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        assertEquals(TestConfiguration.TestCards.VISA_SUCCESS, params.number)
    }

    @Test
    fun testCardParams_validation_acceptsMastercard() {
        val params = CardParams(
            number = TestConfiguration.TestCards.MASTERCARD,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        assertEquals(TestConfiguration.TestCards.MASTERCARD, params.number)
    }

    @Test
    fun testCardParams_validation_acceptsAmex() {
        val params = CardParams(
            number = TestConfiguration.TestCards.AMEX,
            expMonth = 12,
            expYear = 2030,
            cvc = "1234" // Amex uses 4-digit CVC
        )

        assertEquals(TestConfiguration.TestCards.AMEX, params.number)
    }

    @Test
    fun testCardParams_sanitization() {
        val params = CardParams(
            number = "4242 4242 4242 4242",
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        assertEquals("4242424242424242", params.getSanitizedNumber())
    }


    @Test
    fun testPaymentMethodCreateParams_card() {
        val params = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123",
            billingDetails = BillingDetails(
                name = "Test User",
                email = "test@example.com",
                phone = "+1234567890",
                address = Address(
                    line1 = "123 Test St",
                    city = "San Francisco",
                    state = "CA",
                    postalCode = "94111",
                    country = "US"
                )
            )
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertNotNull(params.billingDetails)
        assertEquals("Test User", params.billingDetails?.name)
    }

    @Test
    fun testPaymentMethodCreateParams_fromToken() {
        val params = PaymentMethodCreateParams.createCardFromToken(
            token = "tok_visa"
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertEquals("tok_visa", params.card?.token)
        assertNull(params.card?.number)
    }

    @Test
    fun testPaymentMethodCreateParams_ideal() {
        val params = PaymentMethodCreateParams.createIdeal(
            bank = "abn_amro",
            billingDetails = BillingDetails(name = "Dutch Customer")
        )

        assertEquals(PaymentMethodType.IDEAL, params.type)
    }

    @Test
    fun testPaymentMethodCreateParams_sepaDebit() {
        val params = PaymentMethodCreateParams.createSepaDebit(
            iban = "DE89370400440532013000",
            billingDetails = BillingDetails(
                name = "German Customer",
                email = "customer@example.de"
            )
        )

        assertEquals(PaymentMethodType.SEPA_DEBIT, params.type)
    }


    @Test
    fun testSourceParams_card() {
        val cardParams = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )
        val params = SourceParams.createCardParams(cardParams)

        assertEquals(SourceType.CARD, params.type)
    }

    @Test
    fun testSourceParams_bancontact() {
        val params = SourceParams.createBancontactParams(
            amount = 1000,
            name = "Belgian Customer",
            returnUrl = "https://example.com/return"
        )

        assertEquals(SourceType.BANCONTACT, params.type)
        assertEquals(1000L, params.amount)
        assertEquals("eur", params.currency)
    }

    @Test
    fun testSourceParams_ideal() {
        val params = SourceParams.createIdealParams(
            amount = 2000,
            name = "Dutch Customer",
            returnUrl = "https://example.com/return",
            bank = "abn_amro"
        )

        assertEquals(SourceType.IDEAL, params.type)
        assertEquals(2000L, params.amount)
    }

    @Test
    fun testSourceParams_giropay() {
        val params = SourceParams.createGiropayParams(
            amount = 3000,
            name = "German Customer",
            returnUrl = "https://example.com/return"
        )

        assertEquals(SourceType.GIROPAY, params.type)
    }

    @Test
    fun testSourceParams_sepaDebit() {
        val params = SourceParams.createSepaDebitParams(
            name = "European Customer",
            iban = "DE89370400440532013000",
            email = "customer@example.eu"
        )

        assertEquals(SourceType.SEPA_DEBIT, params.type)
    }


    @Test
    fun testConfirmPaymentIntentParams_withPaymentMethodId() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "pi_test_123_secret_456"
        )

        assertEquals("pm_test_123", params.paymentMethodId)
        assertEquals("pi_test_123_secret_456", params.clientSecret)
        assertNull(params.paymentMethodCreateParams)
    }

    @Test
    fun testConfirmPaymentIntentParams_withPaymentMethodCreateParams() {
        val createParams = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams = createParams,
            clientSecret = "pi_test_789_secret_012"
        )

        assertNotNull(params.paymentMethodCreateParams)
        assertNull(params.paymentMethodId)
    }

    @Test
    fun testConfirmPaymentIntentParams_withShipping() {
        val shipping = ShippingDetails(
            name = "John Doe",
            address = Address(
                line1 = "123 Main St",
                city = "San Francisco",
                state = "CA",
                postalCode = "94111",
                country = "US"
            ),
            carrier = "USPS",
            phone = "+1234567890",
            trackingNumber = "1Z999AA10123456784"
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_456",
            clientSecret = "pi_test_456_secret_789",
            shipping = shipping
        )

        assertNotNull(params.shipping)
        assertEquals("John Doe", params.shipping?.name)
        assertEquals("USPS", params.shipping?.carrier)
    }


    @Test
    fun testConfirmSetupIntentParams_withPaymentMethodId() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_abc",
            clientSecret = "seti_test_abc_secret_def"
        )

        assertEquals("pm_test_abc", params.paymentMethodId)
        assertEquals("seti_test_abc_secret_def", params.clientSecret)
    }

    @Test
    fun testConfirmSetupIntentParams_withPaymentMethodCreateParams() {
        val createParams = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val params = ConfirmSetupIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams = createParams,
            clientSecret = "seti_test_ghi_secret_jkl"
        )

        assertNotNull(params.paymentMethodCreateParams)
        assertNull(params.paymentMethodId)
    }


    @Test
    fun testIdempotencyKey_generation() {
        val key1 = IdempotencyKey.generate()
        val key2 = IdempotencyKey.generate()

        assertNotNull(key1.value)
        assertNotNull(key2.value)
        assertTrue(key1.value != key2.value)
        assertEquals(32, key1.value.length)
    }

    @Test
    fun testIdempotencyKey_fromValue() {
        val key = IdempotencyKey.fromValue("custom-key-12345")
        assertEquals("custom-key-12345", key.value)
    }

    @Test
    fun testIdempotencyKey_rejectsBlank() {
        assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue("")
        }
    }

    @Test
    fun testIdempotencyKey_rejectsTooLong() {
        assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue("a".repeat(256))
        }
    }


    @Test
    fun testStripeResult_success() {
        val result: StripeResult<String> = StripeResult.success("test")
        assertTrue(result.isSuccess())
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun testStripeResult_failure() {
        val error = StripeException("Test error")
        val result: StripeResult<String> = StripeResult.failure(error)
        assertTrue(result.isFailure())
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun testStripeResult_map() {
        val result: StripeResult<Int> = StripeResult.success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun testStripeResult_flatMap() {
        val result: StripeResult<Int> = StripeResult.success(5)
        val flatMapped = result.flatMap { StripeResult.success(it * 2) }
        assertEquals(10, flatMapped.getOrNull())
    }

    @Test
    fun testStripeResult_getOrThrow_success() {
        val result: StripeResult<String> = StripeResult.success("value")
        assertEquals("value", result.getOrThrow())
    }

    @Test
    fun testStripeResult_getOrThrow_failure() {
        val result: StripeResult<String> = StripeResult.failure(StripeException("error"))
        assertFailsWith<StripeException> {
            result.getOrThrow()
        }
    }

    @Test
    fun testStripeResult_onSuccess() {
        var called = false
        val result: StripeResult<String> = StripeResult.success("test")
        result.onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun testStripeResult_onFailure() {
        var called = false
        val result: StripeResult<String> = StripeResult.failure(StripeException("error"))
        result.onFailure { called = true }
        assertTrue(called)
    }


    @Test
    fun testPaymentIntentStatus_allValues() {
        val statuses = listOf(
            "requires_payment_method",
            "requires_confirmation",
            "requires_action",
            "processing",
            "requires_capture",
            "canceled",
            "succeeded"
        )

        statuses.forEach { status ->
            val parsed = PaymentIntentStatus.fromValue(status)
            assertNotNull(parsed, "Status '$status' should parse successfully")
        }
    }

    @Test
    fun testSetupIntentStatus_allValues() {
        val statuses = listOf(
            "requires_payment_method",
            "requires_confirmation",
            "requires_action",
            "processing",
            "canceled",
            "succeeded"
        )

        statuses.forEach { status ->
            val parsed = SetupIntentStatus.fromValue(status)
            assertNotNull(parsed, "Status '$status' should parse successfully")
        }
    }

    @Test
    fun testPaymentMethodType_allValues() {
        val types = listOf(
            "card",
            "card_present",
            "ideal",
            "sepa_debit",
            "au_becs_debit",
            "bacs_debit",
            "bancontact",
            "giropay",
            "p24",
            "eps",
            "sofort"
        )

        types.forEach { type ->
            val parsed = PaymentMethodType.fromValue(type)
            assertTrue(parsed != PaymentMethodType.UNKNOWN || type == "unknown",
                "Type '$type' should parse to a known value")
        }
    }

    @Test
    fun testSourceType_allValues() {
        val types = listOf(
            "card",
            "three_d_secure",
            "giropay",
            "sepa_debit",
            "ideal",
            "sofort",
            "bancontact",
            "alipay",
            "eps",
            "p24",
            "multibanco",
            "wechat"
        )

        types.forEach { type ->
            val parsed = SourceType.fromValue(type)
            assertTrue(parsed != SourceType.UNKNOWN || type == "unknown",
                "Type '$type' should parse to a known value")
        }
    }
}
