package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StripeExceptionTest {
    @Test
    fun testStripeException() {
        val error = StripeError(
            type = "card_error",
            code = "card_declined",
            message = "Your card was declined"
        )

        val exception = StripeException(
            message = "Payment failed",
            stripeError = error,
            statusCode = 402,
            requestId = "req_123"
        )

        assertEquals("Payment failed", exception.message)
        assertEquals(error, exception.stripeError)
        assertEquals(402, exception.statusCode)
        assertEquals("req_123", exception.requestId)
    }

    @Test
    fun testCardException() {
        val exception = CardException(
            message = "Card declined",
            declineCode = "insufficient_funds",
            charge = "ch_123"
        )

        assertEquals("Card declined", exception.message)
        assertEquals("insufficient_funds", exception.declineCode)
        assertEquals("ch_123", exception.charge)
    }

    @Test
    fun testInvalidRequestException() {
        val exception = InvalidRequestException(
            message = "Invalid request",
            param = "amount"
        )

        assertEquals("Invalid request", exception.message)
        assertEquals("amount", exception.param)
    }

    @Test
    fun testAuthenticationException() {
        val exception = AuthenticationException(
            message = "Invalid API key"
        )

        assertEquals("Invalid API key", exception.message)
    }

    @Test
    fun testAPIConnectionException() {
        val exception = APIConnectionException(
            message = "Network error"
        )

        assertEquals("Network error", exception.message)
    }

    @Test
    fun testRateLimitException() {
        val exception = RateLimitException(
            message = "Too many requests"
        )

        assertEquals("Too many requests", exception.message)
    }
}

class StripeErrorCodeTest {
    @Test
    fun testErrorCodeFromValue() {
        assertEquals(StripeErrorCode.CARD_DECLINED, StripeErrorCode.fromCode("card_declined"))
        assertEquals(StripeErrorCode.INSUFFICIENT_FUNDS, StripeErrorCode.fromCode("insufficient_funds"))
        assertEquals(StripeErrorCode.EXPIRED_CARD, StripeErrorCode.fromCode("expired_card"))
        assertEquals(StripeErrorCode.UNKNOWN, StripeErrorCode.fromCode("invalid_code"))
    }

    @Test
    fun testErrorCodeValues() {
        assertEquals("card_declined", StripeErrorCode.CARD_DECLINED.code)
        assertEquals("insufficient_funds", StripeErrorCode.INSUFFICIENT_FUNDS.code)
        assertEquals("rate_limit", StripeErrorCode.RATE_LIMIT.code)
    }
}
