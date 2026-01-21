package com.jakebarnby.stripe.api

import com.jakebarnby.stripe.StripeResult
import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

/**
 * Tests for error handling across the Stripe API client.
 */
class ErrorHandlingTest {


    @Test
    fun testStripeException_withMessageOnly() {
        val exception = StripeException("Card declined")
        assertEquals("Card declined", exception.message)
        assertNull(exception.stripeError)
        assertNull(exception.statusCode)
        assertNull(exception.requestId)
        assertNull(exception.cause)
    }

    @Test
    fun testStripeException_withStripeError() {
        val stripeError = StripeError(
            type = "card_error",
            code = "card_declined",
            message = "Your card was declined",
            declineCode = "insufficient_funds"
        )
        val exception = StripeException(
            message = "Your card was declined",
            stripeError = stripeError,
            statusCode = 402
        )
        assertEquals("Your card was declined", exception.message)
        assertEquals("card_declined", exception.stripeError?.code)
        assertEquals("insufficient_funds", exception.stripeError?.declineCode)
        assertEquals(402, exception.statusCode)
    }

    @Test
    fun testStripeException_withCause() {
        val cause = RuntimeException("Network error")
        val exception = StripeException("Request failed", cause = cause)
        assertEquals("Request failed", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun testStripeError_commonDeclineCodes() {
        val declineCodes = listOf(
            "insufficient_funds",
            "lost_card",
            "stolen_card",
            "expired_card",
            "incorrect_cvc",
            "processing_error",
            "incorrect_number",
            "card_velocity_exceeded",
            "do_not_honor",
            "generic_decline"
        )

        declineCodes.forEach { declineCode ->
            val error = StripeError(
                type = "card_error",
                code = "card_declined",
                message = "Card declined: $declineCode",
                declineCode = declineCode
            )
            assertEquals(declineCode, error.declineCode)
        }
    }

    @Test
    fun testStripeError_commonErrorCodes() {
        val errorCodes = listOf(
            "card_declined",
            "expired_card",
            "incorrect_cvc",
            "incorrect_number",
            "incorrect_zip",
            "invalid_expiry_month",
            "invalid_expiry_year",
            "invalid_number",
            "missing",
            "processing_error",
            "rate_limit"
        )

        errorCodes.forEach { code ->
            val error = StripeError(
                type = "card_error",
                code = code,
                message = "Error: $code"
            )
            assertEquals(code, error.code)
        }
    }


    @Test
    fun testStripeResult_failure_preservesException() {
        val stripeError = StripeError(
            type = "card_error",
            code = "test_code",
            message = "Test error"
        )
        val exception = StripeException("Test error", stripeError = stripeError)
        val result: StripeResult<String> = StripeResult.failure(exception)

        assertTrue(result.isFailure())
        assertFalse(result.isSuccess())
        assertNull(result.getOrNull())
        assertEquals(exception, result.errorOrNull())
        assertEquals("test_code", result.errorOrNull()?.stripeError?.code)
    }

    @Test
    fun testStripeResult_getOrThrow_throwsOriginalException() {
        val stripeError = StripeError(
            type = "card_error",
            code = "original_code",
            message = "Original error"
        )
        val exception = StripeException("Original error", stripeError = stripeError)
        val result: StripeResult<String> = StripeResult.failure(exception)

        val thrown = assertFailsWith<StripeException> {
            result.getOrThrow()
        }
        assertEquals("Original error", thrown.message)
        assertEquals("original_code", thrown.stripeError?.code)
    }

    @Test
    fun testStripeResult_map_doesNotExecuteOnFailure() {
        var executed = false
        val result: StripeResult<String> = StripeResult.failure(StripeException("Error"))
        result.map {
            executed = true
            it.uppercase()
        }
        assertFalse(executed)
    }

    @Test
    fun testStripeResult_flatMap_doesNotExecuteOnFailure() {
        var executed = false
        val result: StripeResult<String> = StripeResult.failure(StripeException("Error"))
        result.flatMap {
            executed = true
            StripeResult.success(it.uppercase())
        }
        assertFalse(executed)
    }

    @Test
    fun testStripeResult_onSuccess_doesNotExecuteOnFailure() {
        var executed = false
        val result: StripeResult<String> = StripeResult.failure(StripeException("Error"))
        result.onSuccess { executed = true }
        assertFalse(executed)
    }

    @Test
    fun testStripeResult_onFailure_executesOnFailure() {
        var capturedError: StripeException? = null
        val exception = StripeException("Test error")
        val result: StripeResult<String> = StripeResult.failure(exception)
        result.onFailure { capturedError = it }
        assertEquals(exception, capturedError)
    }

    @Test
    fun testStripeResult_runCatching_capturesException() {
        val result = StripeResult.runCatching<String> {
            throw StripeException("Caught exception")
        }

        assertTrue(result.isFailure())
        assertEquals("Caught exception", result.errorOrNull()?.message)
    }

    @Test
    fun testStripeResult_runCatching_capturesNonStripeException() {
        val result = StripeResult.runCatching<String> {
            throw RuntimeException("Runtime error")
        }

        assertTrue(result.isFailure())
        assertNotNull(result.errorOrNull())
    }


    @Test
    fun testPaymentIntentError_creation() {
        val error = PaymentIntentError(
            type = "card_error",
            code = "card_declined",
            declineCode = "generic_decline",
            message = "Your card was declined"
        )

        assertEquals("card_error", error.type)
        assertEquals("card_declined", error.code)
        assertEquals("generic_decline", error.declineCode)
        assertEquals("Your card was declined", error.message)
    }

    @Test
    fun testPaymentIntentError_withNullFields() {
        val error = PaymentIntentError(
            type = "api_error",
            code = null,
            declineCode = null,
            message = "An error occurred"
        )

        assertEquals("api_error", error.type)
        assertNull(error.code)
        assertNull(error.declineCode)
    }


    @Test
    fun testSetupIntentError_creation() {
        val error = SetupIntentError(
            type = "card_error",
            code = "card_declined",
            declineCode = "generic_decline",
            message = "Your card was declined"
        )

        assertEquals("card_error", error.type)
        assertEquals("card_declined", error.code)
        assertEquals("generic_decline", error.declineCode)
    }


    @Test
    fun testClientSecret_extractsPaymentIntentId() {
        val clientSecret = "pi_3MtweELkdIwHu7ix0Dt0Gxqc_secret_YrZHLfGUcRsYcJk0BqBa4xAJp"
        val intentId = clientSecret.substringBefore("_secret_")
        assertEquals("pi_3MtweELkdIwHu7ix0Dt0Gxqc", intentId)
    }

    @Test
    fun testClientSecret_extractsSetupIntentId() {
        val clientSecret = "seti_1MtweGLkdIwHu7ixlDxxO6Uu_secret_NXTcghvyZDYw19VbdVYJHV5mhxwmrcC"
        val intentId = clientSecret.substringBefore("_secret_")
        assertEquals("seti_1MtweGLkdIwHu7ixlDxxO6Uu", intentId)
    }

    @Test
    fun testClientSecret_handlesInvalidFormat() {
        val invalidSecret = "invalid_secret_format"
        val result = invalidSecret.substringBefore("_secret_")
        assertEquals("invalid", result)
    }


    @Test
    fun testCardException_withDeclineCode() {
        val exception = CardException(
            message = "Card was declined",
            declineCode = "insufficient_funds",
            charge = "ch_123"
        )
        assertEquals("Card was declined", exception.message)
        assertEquals("insufficient_funds", exception.declineCode)
        assertEquals("ch_123", exception.charge)
    }


    @Test
    fun testInvalidRequestException_withParam() {
        val exception = InvalidRequestException(
            message = "Invalid parameter",
            param = "amount",
            statusCode = 400
        )
        assertEquals("Invalid parameter", exception.message)
        assertEquals("amount", exception.param)
        assertEquals(400, exception.statusCode)
    }


    @Test
    fun testAuthenticationException_invalidApiKey() {
        val exception = AuthenticationException(
            message = "Invalid API key provided",
            statusCode = 401
        )
        assertEquals("Invalid API key provided", exception.message)
        assertEquals(401, exception.statusCode)
    }


    @Test
    fun testRateLimitException_creation() {
        val exception = RateLimitException(
            message = "Rate limit exceeded",
            statusCode = 429,
            requestId = "req_abc123"
        )
        assertEquals("Rate limit exceeded", exception.message)
        assertEquals(429, exception.statusCode)
        assertEquals("req_abc123", exception.requestId)
    }


    @Test
    fun testAPIConnectionException_withCause() {
        val networkCause = RuntimeException("Connection timed out")
        val exception = APIConnectionException(
            message = "Network request failed",
            cause = networkCause
        )

        assertEquals("Network request failed", exception.message)
        assertEquals("Connection timed out", exception.cause?.message)
    }


    @Test
    fun testStripeErrorCode_fromCode_knownCodes() {
        assertEquals(StripeErrorCode.CARD_DECLINED, StripeErrorCode.fromCode("card_declined"))
        assertEquals(StripeErrorCode.EXPIRED_CARD, StripeErrorCode.fromCode("expired_card"))
        assertEquals(StripeErrorCode.INSUFFICIENT_FUNDS, StripeErrorCode.fromCode("insufficient_funds"))
        assertEquals(StripeErrorCode.RATE_LIMIT, StripeErrorCode.fromCode("rate_limit"))
    }

    @Test
    fun testStripeErrorCode_fromCode_unknownCode() {
        assertEquals(StripeErrorCode.UNKNOWN, StripeErrorCode.fromCode("some_random_code"))
    }

    @Test
    fun testStripeErrorCode_codeProperty() {
        assertEquals("card_declined", StripeErrorCode.CARD_DECLINED.code)
        assertEquals("insufficient_funds", StripeErrorCode.INSUFFICIENT_FUNDS.code)
    }
}
