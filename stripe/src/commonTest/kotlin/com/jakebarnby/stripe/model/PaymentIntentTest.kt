package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PaymentIntentTest {
    @Test
    fun testPaymentIntentCreation() {
        val paymentIntent = PaymentIntent(
            id = "pi_123",
            clientSecret = "pi_123_secret_abc",
            amount = 1000,
            currency = "usd",
            status = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
            created = 1234567890,
            livemode = false
        )

        assertEquals("pi_123", paymentIntent.id)
        assertEquals("pi_123_secret_abc", paymentIntent.clientSecret)
        assertEquals(1000, paymentIntent.amount)
        assertEquals("usd", paymentIntent.currency)
        assertEquals(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, paymentIntent.status)
    }

    @Test
    fun testPaymentIntentValidation() {
        assertFailsWith<IllegalArgumentException> {
            PaymentIntent(
                id = "",
                clientSecret = "pi_123_secret_abc",
                amount = 1000,
                currency = "usd",
                status = PaymentIntentStatus.SUCCEEDED,
                created = 1234567890,
                livemode = false
            )
        }

        assertFailsWith<IllegalArgumentException> {
            PaymentIntent(
                id = "pi_123",
                clientSecret = "pi_123_secret_abc",
                amount = 1000,
                currency = "dollar",
                status = PaymentIntentStatus.SUCCEEDED,
                created = 1234567890,
                livemode = false
            )
        }
    }

    @Test
    fun testPaymentIntentStatusEnum() {
        assertEquals(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, PaymentIntentStatus.fromValue("requires_payment_method"))
        assertEquals(PaymentIntentStatus.SUCCEEDED, PaymentIntentStatus.fromValue("succeeded"))
        assertEquals(PaymentIntentStatus.CANCELED, PaymentIntentStatus.fromValue("canceled"))
    }
}

class ConfirmPaymentIntentParamsTest {
    @Test
    fun testConfirmPaymentIntentParamsWithPaymentMethodId() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_123",
            clientSecret = "pi_123_secret_abc"
        )

        assertEquals("pi_123_secret_abc", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertNull(params.paymentMethodCreateParams)
    }

    @Test
    fun testConfirmPaymentIntentParamsWithCreateParams() {
        val createParams = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams = createParams,
            clientSecret = "pi_123_secret_abc"
        )

        assertEquals("pi_123_secret_abc", params.clientSecret)
        assertNull(params.paymentMethodId)
        assertNotNull(params.paymentMethodCreateParams)
    }

    @Test
    fun testConfirmPaymentIntentParamsBuilder() {
        val params = ConfirmPaymentIntentParams.builder("pi_123_secret_abc")
            .paymentMethodId("pm_123")
            .returnUrl("https://example.com/return")
            .setupFutureUsage(SetupFutureUsage.OFF_SESSION)
            .build()

        assertEquals("pi_123_secret_abc", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertEquals("https://example.com/return", params.returnUrl)
        assertEquals(SetupFutureUsage.OFF_SESSION, params.setupFutureUsage)
    }
}

class NextActionTest {
    @Test
    fun testNextActionWithRedirect() {
        val redirect = RedirectToUrl(
            url = "https://stripe.com/redirect",
            returnUrl = "https://example.com/return"
        )

        val nextAction = NextAction(
            type = NextActionType.REDIRECT_TO_URL,
            redirectToUrl = redirect
        )

        assertEquals(NextActionType.REDIRECT_TO_URL, nextAction.type)
        assertNotNull(nextAction.redirectToUrl)
        assertEquals("https://stripe.com/redirect", nextAction.redirectToUrl?.url)
    }

    @Test
    fun testNextActionValidation() {
        assertFailsWith<IllegalArgumentException> {
            NextAction(
                type = NextActionType.REDIRECT_TO_URL,
                redirectToUrl = null
            )
        }
    }

    @Test
    fun testNextActionTypeEnum() {
        assertEquals(NextActionType.REDIRECT_TO_URL, NextActionType.fromValue("redirect_to_url"))
        assertEquals(NextActionType.USE_STRIPE_SDK, NextActionType.fromValue("use_stripe_sdk"))
        assertEquals(NextActionType.DISPLAY_OXXO_DETAILS, NextActionType.fromValue("display_oxxo_details"))
    }
}

class CaptureMethodTest {
    @Test
    fun testCaptureMethodEnum() {
        assertEquals(CaptureMethod.AUTOMATIC, CaptureMethod.fromValue("automatic"))
        assertEquals(CaptureMethod.MANUAL, CaptureMethod.fromValue("manual"))
    }
}

class ConfirmationMethodTest {
    @Test
    fun testConfirmationMethodEnum() {
        assertEquals(ConfirmationMethod.AUTOMATIC, ConfirmationMethod.fromValue("automatic"))
        assertEquals(ConfirmationMethod.MANUAL, ConfirmationMethod.fromValue("manual"))
    }
}

class SetupFutureUsageTest {
    @Test
    fun testSetupFutureUsageEnum() {
        assertEquals(SetupFutureUsage.ON_SESSION, SetupFutureUsage.fromValue("on_session"))
        assertEquals(SetupFutureUsage.OFF_SESSION, SetupFutureUsage.fromValue("off_session"))
    }
}
