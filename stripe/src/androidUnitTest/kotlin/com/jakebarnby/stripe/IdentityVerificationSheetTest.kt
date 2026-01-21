package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdentityVerificationSheetTest {

    @Test
    fun testConfigurationCreation() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_1234567890",
            ephemeralKeySecret = "ek_test_secret123",
            brandLogo = "https://example.com/logo.png"
        )

        assertEquals("vs_1234567890", config.verificationSessionId)
        assertEquals("https://example.com/logo.png", config.brandLogo)
    }

    @Test
    fun testConfigurationWithoutBrandLogo() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_1234567890",
            ephemeralKeySecret = "ek_test_secret123"
        )

        assertEquals("vs_1234567890", config.verificationSessionId)
        assertEquals(null, config.brandLogo)
    }

    @Test
    fun testConfigurationInvalidSessionIdFormat() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSheetConfiguration(
                verificationSessionId = "invalid_format",
                ephemeralKeySecret = "ek_test_secret123"
            )
        }
    }

    @Test
    fun testConfigurationInvalidEphemeralKeyFormat() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSheetConfiguration(
                verificationSessionId = "vs_1234567890",
                ephemeralKeySecret = "invalid_format"
            )
        }
    }

    @Test
    fun testConfigurationRedactsEphemeralKey() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_1234567890",
            ephemeralKeySecret = "ek_test_secret123"
        )

        val toString = config.toString()
        assertTrue(toString.contains("REDACTED"))
        assertTrue(!toString.contains("ek_test_secret123"))
        assertTrue(toString.contains("vs_1234567890"))
    }

    @Test
    fun testResultCompleted() {
        val session = IdentityVerificationSession(
            id = "vs_1234567890",
            clientSecret = "vs_1234567890_secret_abc",
            status = VerificationSessionStatus.VERIFIED,
            type = VerificationType.DOCUMENT,
            created = 1234567890
        )

        val result = IdentityVerificationSheetResult.Completed(session)
        assertTrue(result is IdentityVerificationSheetResult.Completed)
        assertEquals(session, result.session)
        assertEquals(VerificationSessionStatus.VERIFIED, result.session.status)
    }

    @Test
    fun testResultCanceled() {
        val result = IdentityVerificationSheetResult.Canceled
        assertTrue(result is IdentityVerificationSheetResult.Canceled)
    }

    @Test
    fun testResultFailed() {
        val exception = StripeException(
            message = "Verification failed",
            statusCode = 400
        )

        val result = IdentityVerificationSheetResult.Failed(exception)
        assertTrue(result is IdentityVerificationSheetResult.Failed)
        assertEquals("Verification failed", result.error.message)
        assertEquals(400, result.error.statusCode)
    }

    @Test
    fun testResultIsSealed() {
        // Test that we can exhaustively match on the sealed class
        val result: IdentityVerificationSheetResult = IdentityVerificationSheetResult.Canceled

        val handled = when (result) {
            is IdentityVerificationSheetResult.Completed -> "completed"
            is IdentityVerificationSheetResult.Canceled -> "canceled"
            is IdentityVerificationSheetResult.Failed -> "failed"
        }

        assertEquals("canceled", handled)
    }

    @Test
    fun testResultCompletedWithError() {
        // Test that a completed session can still contain an error
        val error = VerificationSessionError(
            code = VerificationErrorCode.DOCUMENT_EXPIRED,
            reason = "Document has expired"
        )

        val session = IdentityVerificationSession(
            id = "vs_1234567890",
            clientSecret = "vs_1234567890_secret_abc",
            status = VerificationSessionStatus.CANCELED,
            type = VerificationType.DOCUMENT,
            lastError = error,
            created = 1234567890
        )

        val result = IdentityVerificationSheetResult.Completed(session)
        assertNotNull(result.session.lastError)
        assertEquals(VerificationErrorCode.DOCUMENT_EXPIRED, result.session.lastError.code)
        assertEquals("Document has expired", result.session.lastError.reason)
    }

    @Test
    fun testResultCompletedWithVerifiedOutputs() {
        val outputs = VerifiedOutputs(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = DateOfBirth(15, 6, 1990),
            address = Address(
                line1 = "123 Main St",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            ),
            idNumber = "123-45-6789",
            idNumberType = IdNumberType.US_SSN
        )

        val session = IdentityVerificationSession(
            id = "vs_1234567890",
            clientSecret = "vs_1234567890_secret_abc",
            status = VerificationSessionStatus.VERIFIED,
            type = VerificationType.DOCUMENT,
            verifiedOutputs = outputs,
            created = 1234567890
        )

        val result = IdentityVerificationSheetResult.Completed(session)
        assertNotNull(result.session.verifiedOutputs)
        assertEquals("John", result.session.verifiedOutputs.firstName)
        assertEquals("Doe", result.session.verifiedOutputs.lastName)
        assertEquals(15, result.session.verifiedOutputs.dateOfBirth?.day)
    }

    @Test
    fun testResultFailedWithDifferentExceptionTypes() {
        // Test CardException
        val cardException = CardException(
            message = "Card declined",
            declineCode = "insufficient_funds"
        )
        val cardResult = IdentityVerificationSheetResult.Failed(cardException)
        assertEquals("insufficient_funds", (cardResult.error as CardException).declineCode)

        // Test InvalidRequestException
        val invalidRequestException = InvalidRequestException(
            message = "Invalid parameter",
            param = "verification_session_id"
        )
        val invalidResult = IdentityVerificationSheetResult.Failed(invalidRequestException)
        assertEquals("verification_session_id", (invalidResult.error as InvalidRequestException).param)

        // Test AuthenticationException
        val authException = AuthenticationException(
            message = "Invalid API key"
        )
        val authResult = IdentityVerificationSheetResult.Failed(authException)
        assertTrue(authResult.error is AuthenticationException)
    }

    @Test
    fun testConfigurationWithLongIds() {
        // Test with realistic long IDs
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_1A2B3C4D5E6F7G8H9I0J",
            ephemeralKeySecret = "ek_test_51A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P7Q8R9S0T"
        )

        assertNotNull(config)
        assertEquals("vs_1A2B3C4D5E6F7G8H9I0J", config.verificationSessionId)
    }

    @Test
    fun testMultipleVerificationTypes() {
        val types = listOf(
            VerificationType.DOCUMENT,
            VerificationType.ID_NUMBER,
            VerificationType.ADDRESS
        )

        types.forEach { type ->
            val session = IdentityVerificationSession(
                id = "vs_test_$type",
                clientSecret = "vs_test_${type}_secret_abc",
                status = VerificationSessionStatus.REQUIRES_INPUT,
                type = type,
                created = 1234567890
            )

            assertEquals(type, session.type)
        }
    }

    @Test
    fun testAllVerificationStatuses() {
        val statuses = listOf(
            VerificationSessionStatus.REQUIRES_INPUT,
            VerificationSessionStatus.PROCESSING,
            VerificationSessionStatus.VERIFIED,
            VerificationSessionStatus.CANCELED
        )

        statuses.forEach { status ->
            val session = IdentityVerificationSession(
                id = "vs_test_$status",
                clientSecret = "vs_test_${status}_secret_abc",
                status = status,
                type = VerificationType.DOCUMENT,
                created = 1234567890
            )

            assertEquals(status, session.status)
        }
    }

    @Test
    fun testSessionWithMetadata() {
        val metadata = mapOf(
            "user_id" to "user_123",
            "application_id" to "app_456",
            "session_token" to "token_789"
        )

        val session = IdentityVerificationSession(
            id = "vs_test_metadata",
            clientSecret = "vs_test_metadata_secret_abc",
            status = VerificationSessionStatus.REQUIRES_INPUT,
            type = VerificationType.DOCUMENT,
            created = 1234567890,
            metadata = metadata
        )

        assertEquals(3, session.metadata.size)
        assertEquals("user_123", session.metadata["user_id"])
        assertEquals("app_456", session.metadata["application_id"])
        assertEquals("token_789", session.metadata["session_token"])
    }

    @Test
    fun testSessionLivemodeFlag() {
        val livemodeSession = IdentityVerificationSession(
            id = "vs_live",
            clientSecret = "vs_live_secret_abc",
            status = VerificationSessionStatus.REQUIRES_INPUT,
            type = VerificationType.DOCUMENT,
            livemode = true,
            created = 1234567890
        )

        val testmodeSession = IdentityVerificationSession(
            id = "vs_test",
            clientSecret = "vs_test_secret_abc",
            status = VerificationSessionStatus.REQUIRES_INPUT,
            type = VerificationType.DOCUMENT,
            livemode = false,
            created = 1234567890
        )

        assertTrue(livemodeSession.livemode)
        assertTrue(!testmodeSession.livemode)
    }
}
