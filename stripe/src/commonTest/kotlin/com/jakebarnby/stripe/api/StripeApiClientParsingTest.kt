package com.jakebarnby.stripe.api

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

/**
 * Tests for API response parsing logic.
 * These tests validate JSON response parsing without making actual HTTP requests.
 */
class StripeApiClientParsingTest {


    @Test
    fun testToken_creation_withAllFields() {
        val token = Token(
            id = "tok_test_123",
            type = "card",
            created = 1234567890L,
            livemode = false,
            used = false,
            card = CardToken(
                id = "card_123",
                brand = "visa",
                last4 = "4242",
                expMonth = 12,
                expYear = 2025,
                funding = "credit",
                country = "US"
            )
        )

        assertEquals("tok_test_123", token.id)
        assertEquals("card", token.type)
        assertEquals(1234567890L, token.created)
        assertEquals(false, token.livemode)
        assertEquals(false, token.used)
        assertNotNull(token.card)
        assertEquals("visa", token.card?.brand)
        assertEquals("4242", token.card?.last4)
    }

    @Test
    fun testToken_creation_withMinimalFields() {
        val token = Token(
            id = "tok_test_456",
            type = "card",
            created = 1L,
            livemode = false,
            used = false
        )

        assertEquals("tok_test_456", token.id)
        assertNull(token.card)
        assertNull(token.bankAccount)
    }

    @Test
    fun testCardToken_creation_withAllFields() {
        val cardToken = CardToken(
            id = "card_test_789",
            brand = "mastercard",
            last4 = "5555",
            expMonth = 6,
            expYear = 2027,
            funding = "debit",
            country = "GB"
        )

        assertEquals("card_test_789", cardToken.id)
        assertEquals("mastercard", cardToken.brand)
        assertEquals("5555", cardToken.last4)
        assertEquals(6, cardToken.expMonth)
        assertEquals(2027, cardToken.expYear)
        assertEquals("debit", cardToken.funding)
        assertEquals("GB", cardToken.country)
    }

    @Test
    fun testBankAccountToken_creation_withAllFields() {
        val bankAccountToken = BankAccountToken(
            id = "ba_test_abc",
            country = "US",
            currency = "usd",
            last4 = "6789",
            bankName = "Chase",
            accountHolderName = "John Doe",
            accountHolderType = "individual",
            routingNumber = "110000000"
        )

        assertEquals("ba_test_abc", bankAccountToken.id)
        assertEquals("US", bankAccountToken.country)
        assertEquals("usd", bankAccountToken.currency)
        assertEquals("6789", bankAccountToken.last4)
        assertEquals("Chase", bankAccountToken.bankName)
        assertEquals("John Doe", bankAccountToken.accountHolderName)
    }


    @Test
    fun testSource_creation_withAllFields() {
        val source = Source(
            id = "src_test_123",
            type = SourceType.CARD,
            status = SourceStatus.CHARGEABLE,
            amount = 1000L,
            currency = "usd",
            clientSecret = "src_client_secret_123",
            flow = SourceFlow.NONE,
            created = 1234567890L,
            livemode = false
        )

        assertEquals("src_test_123", source.id)
        assertEquals(SourceType.CARD, source.type)
        assertEquals(SourceStatus.CHARGEABLE, source.status)
        assertEquals(1000L, source.amount)
        assertEquals("usd", source.currency)
        assertEquals("src_client_secret_123", source.clientSecret)
    }

    @Test
    fun testSourceType_fromValue_convertsAllTypes() {
        assertEquals(SourceType.CARD, SourceType.fromValue("card"))
        assertEquals(SourceType.THREE_D_SECURE, SourceType.fromValue("three_d_secure"))
        assertEquals(SourceType.GIROPAY, SourceType.fromValue("giropay"))
        assertEquals(SourceType.SEPA_DEBIT, SourceType.fromValue("sepa_debit"))
        assertEquals(SourceType.IDEAL, SourceType.fromValue("ideal"))
        assertEquals(SourceType.SOFORT, SourceType.fromValue("sofort"))
        assertEquals(SourceType.BANCONTACT, SourceType.fromValue("bancontact"))
        assertEquals(SourceType.ALIPAY, SourceType.fromValue("alipay"))
        assertEquals(SourceType.EPS, SourceType.fromValue("eps"))
        assertEquals(SourceType.P24, SourceType.fromValue("p24"))
        assertEquals(SourceType.MULTIBANCO, SourceType.fromValue("multibanco"))
        assertEquals(SourceType.WECHAT, SourceType.fromValue("wechat"))
        assertEquals(SourceType.UNKNOWN, SourceType.fromValue("unknown_type"))
    }

    @Test
    fun testSourceStatus_fromValue_convertsAllStatuses() {
        assertEquals(SourceStatus.PENDING, SourceStatus.fromValue("pending"))
        assertEquals(SourceStatus.CHARGEABLE, SourceStatus.fromValue("chargeable"))
        assertEquals(SourceStatus.CONSUMED, SourceStatus.fromValue("consumed"))
        assertEquals(SourceStatus.CANCELED, SourceStatus.fromValue("canceled"))
        assertEquals(SourceStatus.FAILED, SourceStatus.fromValue("failed"))
        assertNull(SourceStatus.fromValue("invalid_status"))
    }

    @Test
    fun testSourceFlow_fromValue_convertsAllFlows() {
        assertEquals(SourceFlow.REDIRECT, SourceFlow.fromValue("redirect"))
        assertEquals(SourceFlow.RECEIVER, SourceFlow.fromValue("receiver"))
        assertEquals(SourceFlow.CODE_VERIFICATION, SourceFlow.fromValue("code_verification"))
        assertEquals(SourceFlow.NONE, SourceFlow.fromValue("none"))
        assertNull(SourceFlow.fromValue("invalid_flow"))
    }


    @Test
    fun testPaymentMethod_creation_withCard() {
        val paymentMethod = PaymentMethod(
            id = "pm_test_123",
            type = PaymentMethodType.CARD,
            created = 1234567890L,
            livemode = false,
            customer = "cus_test_456",
            card = Card(
                brand = CardBrand.VISA,
                last4 = "4242",
                expMonth = 12,
                expYear = 2025,
                funding = CardFunding.CREDIT,
                country = "US"
            ),
            billingDetails = BillingDetails(
                name = "John Doe",
                email = "john@example.com"
            )
        )

        assertEquals("pm_test_123", paymentMethod.id)
        assertEquals(PaymentMethodType.CARD, paymentMethod.type)
        assertNotNull(paymentMethod.card)
        assertEquals(CardBrand.VISA, paymentMethod.card?.brand)
        assertEquals("4242", paymentMethod.card?.last4)
    }

    @Test
    fun testPaymentMethodType_fromValue_convertsAllTypes() {
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.fromValue("card"))
        assertEquals(PaymentMethodType.CARD_PRESENT, PaymentMethodType.fromValue("card_present"))
        assertEquals(PaymentMethodType.IDEAL, PaymentMethodType.fromValue("ideal"))
        assertEquals(PaymentMethodType.SEPA_DEBIT, PaymentMethodType.fromValue("sepa_debit"))
        assertEquals(PaymentMethodType.AU_BECS_DEBIT, PaymentMethodType.fromValue("au_becs_debit"))
        assertEquals(PaymentMethodType.BACS_DEBIT, PaymentMethodType.fromValue("bacs_debit"))
        assertEquals(PaymentMethodType.BANCONTACT, PaymentMethodType.fromValue("bancontact"))
        assertEquals(PaymentMethodType.GIROPAY, PaymentMethodType.fromValue("giropay"))
        assertEquals(PaymentMethodType.P24, PaymentMethodType.fromValue("p24"))
        assertEquals(PaymentMethodType.EPS, PaymentMethodType.fromValue("eps"))
        assertEquals(PaymentMethodType.SOFORT, PaymentMethodType.fromValue("sofort"))
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.fromValue("unknown_type"))
    }

    @Test
    fun testCardBrand_fromValue_convertsAllBrands() {
        assertEquals(CardBrand.VISA, CardBrand.fromValue("visa"))
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromValue("mastercard"))
        assertEquals(CardBrand.AMERICAN_EXPRESS, CardBrand.fromValue("amex"))
        assertEquals(CardBrand.DISCOVER, CardBrand.fromValue("discover"))
        assertEquals(CardBrand.JCB, CardBrand.fromValue("jcb"))
        assertEquals(CardBrand.DINERS_CLUB, CardBrand.fromValue("diners"))
        assertEquals(CardBrand.UNION_PAY, CardBrand.fromValue("unionpay"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("unknown_brand"))
    }

    @Test
    fun testCardFunding_fromValue_convertsAllTypes() {
        assertEquals(CardFunding.CREDIT, CardFunding.fromValue("credit"))
        assertEquals(CardFunding.DEBIT, CardFunding.fromValue("debit"))
        assertEquals(CardFunding.PREPAID, CardFunding.fromValue("prepaid"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("unknown"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("invalid"))
    }


    @Test
    fun testPaymentIntent_creation_withAllFields() {
        val paymentIntent = PaymentIntent(
            id = "pi_test_123",
            clientSecret = "pi_test_123_secret_456",
            amount = 1000L,
            currency = "usd",
            status = PaymentIntentStatus.SUCCEEDED,
            created = 1234567890L,
            livemode = false,
            paymentMethodId = "pm_test_789",
            captureMethod = CaptureMethod.AUTOMATIC
        )

        assertEquals("pi_test_123", paymentIntent.id)
        assertEquals("pi_test_123_secret_456", paymentIntent.clientSecret)
        assertEquals(1000L, paymentIntent.amount)
        assertEquals("usd", paymentIntent.currency)
        assertEquals(PaymentIntentStatus.SUCCEEDED, paymentIntent.status)
        assertEquals("pm_test_789", paymentIntent.paymentMethodId)
    }

    @Test
    fun testPaymentIntentStatus_fromValue_convertsAllStatuses() {
        assertEquals(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, PaymentIntentStatus.fromValue("requires_payment_method"))
        assertEquals(PaymentIntentStatus.REQUIRES_CONFIRMATION, PaymentIntentStatus.fromValue("requires_confirmation"))
        assertEquals(PaymentIntentStatus.REQUIRES_ACTION, PaymentIntentStatus.fromValue("requires_action"))
        assertEquals(PaymentIntentStatus.PROCESSING, PaymentIntentStatus.fromValue("processing"))
        assertEquals(PaymentIntentStatus.REQUIRES_CAPTURE, PaymentIntentStatus.fromValue("requires_capture"))
        assertEquals(PaymentIntentStatus.CANCELED, PaymentIntentStatus.fromValue("canceled"))
        assertEquals(PaymentIntentStatus.SUCCEEDED, PaymentIntentStatus.fromValue("succeeded"))
        assertNull(PaymentIntentStatus.fromValue("invalid_status"))
    }

    @Test
    fun testCaptureMethod_fromValue_convertsAllMethods() {
        assertEquals(CaptureMethod.AUTOMATIC, CaptureMethod.fromValue("automatic"))
        assertEquals(CaptureMethod.MANUAL, CaptureMethod.fromValue("manual"))
        assertNull(CaptureMethod.fromValue("invalid_method"))
    }

    @Test
    fun testConfirmationMethod_fromValue_convertsAllMethods() {
        assertEquals(ConfirmationMethod.AUTOMATIC, ConfirmationMethod.fromValue("automatic"))
        assertEquals(ConfirmationMethod.MANUAL, ConfirmationMethod.fromValue("manual"))
        assertNull(ConfirmationMethod.fromValue("invalid_method"))
    }


    @Test
    fun testSetupIntent_creation_withAllFields() {
        val setupIntent = SetupIntent(
            id = "seti_test_123",
            clientSecret = "seti_test_123_secret_456",
            status = SetupIntentStatus.SUCCEEDED,
            created = 1234567890L,
            livemode = false,
            paymentMethodId = "pm_test_789",
            usage = SetupIntentUsage.OFF_SESSION
        )

        assertEquals("seti_test_123", setupIntent.id)
        assertEquals("seti_test_123_secret_456", setupIntent.clientSecret)
        assertEquals(SetupIntentStatus.SUCCEEDED, setupIntent.status)
        assertEquals("pm_test_789", setupIntent.paymentMethodId)
    }

    @Test
    fun testSetupIntentStatus_fromValue_convertsAllStatuses() {
        assertEquals(SetupIntentStatus.REQUIRES_PAYMENT_METHOD, SetupIntentStatus.fromValue("requires_payment_method"))
        assertEquals(SetupIntentStatus.REQUIRES_CONFIRMATION, SetupIntentStatus.fromValue("requires_confirmation"))
        assertEquals(SetupIntentStatus.REQUIRES_ACTION, SetupIntentStatus.fromValue("requires_action"))
        assertEquals(SetupIntentStatus.PROCESSING, SetupIntentStatus.fromValue("processing"))
        assertEquals(SetupIntentStatus.CANCELED, SetupIntentStatus.fromValue("canceled"))
        assertEquals(SetupIntentStatus.SUCCEEDED, SetupIntentStatus.fromValue("succeeded"))
        assertNull(SetupIntentStatus.fromValue("invalid_status"))
    }

    @Test
    fun testSetupIntentUsage_fromValue_convertsAllUsages() {
        assertEquals(SetupIntentUsage.ON_SESSION, SetupIntentUsage.fromValue("on_session"))
        assertEquals(SetupIntentUsage.OFF_SESSION, SetupIntentUsage.fromValue("off_session"))
        assertNull(SetupIntentUsage.fromValue("invalid_usage"))
    }


    @Test
    fun testNextAction_creation_withRedirect() {
        val nextAction = NextAction(
            type = NextActionType.REDIRECT_TO_URL,
            redirectToUrl = RedirectToUrl(
                url = "https://stripe.com/redirect",
                returnUrl = "https://example.com/return"
            )
        )

        assertEquals(NextActionType.REDIRECT_TO_URL, nextAction.type)
        assertNotNull(nextAction.redirectToUrl)
        assertEquals("https://stripe.com/redirect", nextAction.redirectToUrl?.url)
    }

    @Test
    fun testNextActionType_fromValue_convertsAllTypes() {
        assertEquals(NextActionType.REDIRECT_TO_URL, NextActionType.fromValue("redirect_to_url"))
        assertEquals(NextActionType.USE_STRIPE_SDK, NextActionType.fromValue("use_stripe_sdk"))
        assertEquals(NextActionType.DISPLAY_OXXO_DETAILS, NextActionType.fromValue("display_oxxo_details"))
        assertEquals(NextActionType.ALIPAY_HANDLE_REDIRECT, NextActionType.fromValue("alipay_handle_redirect"))
        assertNull(NextActionType.fromValue("invalid_type"))
    }


    @Test
    fun testStripeException_creation_withMessage() {
        val exception = StripeException("Test error message")
        assertEquals("Test error message", exception.message)
    }

    @Test
    fun testStripeException_creation_withCause() {
        val cause = RuntimeException("Root cause")
        val exception = StripeException("Test error", cause = cause)
        assertEquals("Test error", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun testStripeException_creation_withStripeError() {
        val stripeError = StripeError(
            type = "card_error",
            code = "card_declined",
            message = "Card declined",
            declineCode = "generic_decline"
        )
        val exception = StripeException(
            message = "Card declined",
            stripeError = stripeError
        )
        assertEquals("Card declined", exception.message)
        assertEquals("card_declined", exception.stripeError?.code)
        assertEquals("generic_decline", exception.stripeError?.declineCode)
    }


    @Test
    fun testBillingDetails_creation_withAllFields() {
        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            address = Address(
                line1 = "123 Main St",
                line2 = "Apt 4",
                city = "San Francisco",
                state = "CA",
                postalCode = "94111",
                country = "US"
            )
        )

        assertEquals("John Doe", billingDetails.name)
        assertEquals("john@example.com", billingDetails.email)
        assertEquals("+1234567890", billingDetails.phone)
        assertNotNull(billingDetails.address)
        assertEquals("123 Main St", billingDetails.address?.line1)
        assertEquals("US", billingDetails.address?.country)
    }

    @Test
    fun testAddress_creation_withAllFields() {
        val address = Address(
            line1 = "123 Main St",
            line2 = "Suite 100",
            city = "New York",
            state = "NY",
            postalCode = "10001",
            country = "US"
        )

        assertEquals("123 Main St", address.line1)
        assertEquals("Suite 100", address.line2)
        assertEquals("New York", address.city)
        assertEquals("NY", address.state)
        assertEquals("10001", address.postalCode)
        assertEquals("US", address.country)
    }
}
