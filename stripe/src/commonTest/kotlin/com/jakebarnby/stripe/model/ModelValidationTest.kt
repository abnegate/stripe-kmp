package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModelValidationTest {

    @Test
    fun ephemeralKeyCreateParams_rejectsBlankValues() {
        assertFailsWith<IllegalArgumentException> {
            EphemeralKeyCreateParams(customerId = "")
        }
        assertFailsWith<IllegalArgumentException> {
            EphemeralKeyCreateParams(customerId = "cus_123", stripeVersion = "")
        }
    }

    @Test
    fun permissionException_preservesDetails() {
        val stripeError = StripeError(type = "invalid_request_error", message = "Not allowed")
        val exception = PermissionException(
            message = "Missing permissions",
            stripeError = stripeError,
            statusCode = 403,
            requestId = "req_123"
        )

        assertEquals("Missing permissions", exception.message)
        assertEquals(stripeError, exception.stripeError)
        assertEquals(403, exception.statusCode)
        assertEquals("req_123", exception.requestId)
    }

    @Test
    fun setupIntentError_capturesFields() {
        val paymentMethod = PaymentMethod(
            id = "pm_123",
            type = PaymentMethodType.CARD,
            created = 1L,
            livemode = false
        )
        val error = SetupIntentError(
            type = "card_error",
            code = "card_declined",
            declineCode = "generic_decline",
            message = "Card declined",
            paymentMethod = paymentMethod
        )

        assertEquals("card_error", error.type)
        assertEquals("card_declined", error.code)
        assertEquals("generic_decline", error.declineCode)
        assertEquals(paymentMethod, error.paymentMethod)
    }

    @Test
    fun setupNextAction_validatesRequiredFields() {
        val redirect = RedirectToUrl(
            url = "https://stripe.com/redirect",
            returnUrl = "https://example.com/return"
        )
        val redirectAction = SetupNextAction(
            type = SetupNextActionType.REDIRECT_TO_URL,
            redirectToUrl = redirect
        )
        assertEquals(SetupNextActionType.REDIRECT_TO_URL, redirectAction.type)

        val sdkAction = SetupNextAction(
            type = SetupNextActionType.USE_STRIPE_SDK,
            useStripeSdk = mapOf("client_secret" to "seti_secret")
        )
        assertEquals(SetupNextActionType.USE_STRIPE_SDK, sdkAction.type)

        val microdepositAction = SetupNextAction(
            type = SetupNextActionType.VERIFY_WITH_MICRODEPOSITS
        )
        assertEquals(SetupNextActionType.VERIFY_WITH_MICRODEPOSITS, microdepositAction.type)

        assertFailsWith<IllegalArgumentException> {
            SetupNextAction(type = SetupNextActionType.REDIRECT_TO_URL)
        }
        assertFailsWith<IllegalArgumentException> {
            SetupNextAction(type = SetupNextActionType.USE_STRIPE_SDK)
        }
    }

    @Test
    fun nextAction_validatesRequiredFields() {
        val redirectAction = NextAction(
            type = NextActionType.REDIRECT_TO_URL,
            redirectToUrl = RedirectToUrl(
                url = "https://stripe.com/redirect",
                returnUrl = "https://example.com/return"
            )
        )
        assertEquals(NextActionType.REDIRECT_TO_URL, redirectAction.type)

        val sdkAction = NextAction(
            type = NextActionType.USE_STRIPE_SDK,
            useStripeSdk = mapOf("client_secret" to "pi_secret")
        )
        assertEquals(NextActionType.USE_STRIPE_SDK, sdkAction.type)

        assertFailsWith<IllegalArgumentException> {
            NextAction(type = NextActionType.REDIRECT_TO_URL)
        }
        assertFailsWith<IllegalArgumentException> {
            NextAction(type = NextActionType.USE_STRIPE_SDK)
        }
    }

    @Test
    fun customerAcceptance_enforcesValidValues() {
        assertFailsWith<IllegalArgumentException> {
            CustomerAcceptance(type = "invalid", acceptedAt = 1L)
        }
        assertFailsWith<IllegalArgumentException> {
            CustomerAcceptance(type = "online", acceptedAt = 0L)
        }

        val acceptance = CustomerAcceptance(
            type = "online",
            acceptedAt = 123L,
            online = OnlineAcceptance(ipAddress = "127.0.0.1", userAgent = "agent")
        )
        assertEquals("online", acceptance.type)
    }

    @Test
    fun paymentMethod_validatesRequiredFields() {
        assertFailsWith<IllegalArgumentException> {
            PaymentMethod(
                id = "",
                type = PaymentMethodType.CARD,
                created = 1L,
                livemode = false
            )
        }
        assertFailsWith<IllegalArgumentException> {
            PaymentMethod(
                id = "pm_123",
                type = PaymentMethodType.CARD,
                created = 0L,
                livemode = false
            )
        }
    }

    @Test
    fun bankAccountToken_validatesLengths() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountToken(
                id = "ba_123",
                country = "USA",
                currency = "usd",
                last4 = "1234"
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BankAccountToken(
                id = "ba_123",
                country = "US",
                currency = "us",
                last4 = "1234"
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BankAccountToken(
                id = "ba_123",
                country = "US",
                currency = "usd",
                last4 = "123"
            )
        }
    }

    @Test
    fun customer_validatesEmailAndCurrency() {
        assertFailsWith<IllegalArgumentException> {
            Customer(
                id = "cus_123",
                created = 1L,
                livemode = false,
                email = "invalid"
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Customer(
                id = "cus_123",
                created = 1L,
                livemode = false,
                currency = "us"
            )
        }
    }

    @Test
    fun verifiedOutputs_redactsIdNumber() {
        val outputs = VerifiedOutputs(idNumber = "123456789")
        val text = outputs.toString()
        assertTrue(text.contains("***REDACTED***"))
        assertTrue(!text.contains("123456789"))
    }

    @Test
    fun verifiedOutputs_allowsNullIdNumber() {
        val outputs = VerifiedOutputs()
        val text = outputs.toString()
        assertTrue(text.contains("idNumber=null"))
    }
}
