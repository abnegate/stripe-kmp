package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for Stripe API methods.
 * These tests verify that the API method signatures are correctly implemented.
 */
class StripeApiTest {

    @Test
    fun testStripeResultSuccess() {
        val result = StripeResult.success("test value")
        assertTrue(result.isSuccess())
        assertEquals("test value", result.getOrNull())
        assertEquals("test value", result.getOrThrow())
    }

    @Test
    fun testStripeResultFailure() {
        val exception = StripeException("Test error")
        val result = StripeResult.failure<String>(exception)
        assertTrue(result.isFailure())
        assertEquals(null, result.getOrNull())
        assertEquals(exception, result.errorOrNull())
    }

    @Test
    fun testStripeResultMap() {
        val result = StripeResult.success(5)
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isSuccess())
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun testStripeResultFlatMap() {
        val result = StripeResult.success(5)
        val flatMapped = result.flatMap { StripeResult.success(it * 2) }
        assertTrue(flatMapped.isSuccess())
        assertEquals(10, flatMapped.getOrNull())
    }

    @Test
    fun testStripeResultOnSuccess() {
        var called = false
        val result = StripeResult.success("test")
        result.onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun testStripeResultOnFailure() {
        var called = false
        val result = StripeResult.failure<String>(StripeException("error"))
        result.onFailure { called = true }
        assertTrue(called)
    }

    @Test
    fun testCardParamsValidation() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
    }

    @Test
    fun testBankAccountTokenParamsValidation() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000"
        )
        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
        assertEquals("110000000", params.routingNumber)
    }

    @Test
    fun testPiiTokenParamsValidation() {
        val params = PiiTokenParams(personalIdNumber = "000000000")
        assertEquals("000000000", params.personalIdNumber)
    }

    @Test
    fun testPaymentMethodCreateParamsCard() {
        val params = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
    }

    @Test
    fun testConfirmPaymentIntentParamsWithPaymentMethodId() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "pi_test_secret_123"
        )
        assertEquals("pm_test_123", params.paymentMethodId)
        assertEquals("pi_test_secret_123", params.clientSecret)
    }

    @Test
    fun testConfirmSetupIntentParamsWithPaymentMethodId() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "seti_test_secret_123"
        )
        assertEquals("pm_test_123", params.paymentMethodId)
        assertEquals("seti_test_secret_123", params.clientSecret)
    }

    @Test
    fun testSourceParamsCard() {
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
    fun testSourceParamsBancontact() {
        val params = SourceParams.createBancontactParams(
            amount = 1000,
            name = "John Doe",
            returnUrl = "https://example.com/return"
        )
        assertEquals(SourceType.BANCONTACT, params.type)
        assertEquals(1000L, params.amount)
        assertEquals("eur", params.currency)
    }

    @Test
    fun testPaymentIntentStatusEnum() {
        assertEquals("requires_payment_method", PaymentIntentStatus.REQUIRES_PAYMENT_METHOD.value)
        assertEquals("requires_confirmation", PaymentIntentStatus.REQUIRES_CONFIRMATION.value)
        assertEquals("requires_action", PaymentIntentStatus.REQUIRES_ACTION.value)
        assertEquals("processing", PaymentIntentStatus.PROCESSING.value)
        assertEquals("succeeded", PaymentIntentStatus.SUCCEEDED.value)
    }

    @Test
    fun testSetupIntentStatusEnum() {
        assertEquals("requires_payment_method", SetupIntentStatus.REQUIRES_PAYMENT_METHOD.value)
        assertEquals("requires_confirmation", SetupIntentStatus.REQUIRES_CONFIRMATION.value)
        assertEquals("requires_action", SetupIntentStatus.REQUIRES_ACTION.value)
        assertEquals("succeeded", SetupIntentStatus.SUCCEEDED.value)
    }

    @Test
    fun testPaymentMethodTypeEnum() {
        assertEquals("card", PaymentMethodType.CARD.value)
        assertEquals("ideal", PaymentMethodType.IDEAL.value)
        assertEquals("sepa_debit", PaymentMethodType.SEPA_DEBIT.value)
    }

    @Test
    fun testSourceTypeEnum() {
        assertEquals("card", SourceType.CARD.value)
        assertEquals("ideal", SourceType.IDEAL.value)
        assertEquals("bancontact", SourceType.BANCONTACT.value)
    }
}
