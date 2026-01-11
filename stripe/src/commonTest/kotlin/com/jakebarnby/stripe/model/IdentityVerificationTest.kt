package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IdentityVerificationSessionTest {
    @Test
    fun testVerificationSessionCreation() {
        val session = IdentityVerificationSession(
            id = "vs_123",
            clientSecret = "vs_123_secret_abc",
            status = VerificationSessionStatus.REQUIRES_INPUT,
            type = VerificationType.DOCUMENT,
            livemode = true,
            created = 1234567890,
            lastVerificationReport = "vr_123",
            metadata = mapOf("key" to "value")
        )

        assertEquals("vs_123", session.id)
        assertEquals(VerificationSessionStatus.REQUIRES_INPUT, session.status)
        assertEquals(VerificationType.DOCUMENT, session.type)
        assertTrue(session.livemode)
        assertEquals(1234567890, session.created)
        assertEquals("vr_123", session.lastVerificationReport)
        assertEquals("value", session.metadata["key"])
    }

    @Test
    fun testVerificationSessionWithDefaults() {
        val session = IdentityVerificationSession(
            id = "vs_123",
            clientSecret = "vs_123_secret_abc",
            status = VerificationSessionStatus.PROCESSING,
            type = VerificationType.DOCUMENT,
            created = 1234567890
        )

        assertNull(session.lastError)
        assertNull(session.verifiedOutputs)
        assertEquals(false, session.livemode)
        assertNull(session.lastVerificationReport)
        assertTrue(session.metadata.isEmpty())
    }

    @Test
    fun testVerificationSessionIdValidation() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSession(
                id = "invalid_id",
                clientSecret = "vs_123_secret_abc",
                status = VerificationSessionStatus.REQUIRES_INPUT,
                type = VerificationType.DOCUMENT,
                created = 1234567890
            )
        }
    }

    @Test
    fun testVerificationSessionClientSecretValidation() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSession(
                id = "vs_123",
                clientSecret = "invalid_secret",
                status = VerificationSessionStatus.REQUIRES_INPUT,
                type = VerificationType.DOCUMENT,
                created = 1234567890
            )
        }
    }

    @Test
    fun testVerificationSessionCreatedValidation() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSession(
                id = "vs_123",
                clientSecret = "vs_123_secret_abc",
                status = VerificationSessionStatus.REQUIRES_INPUT,
                type = VerificationType.DOCUMENT,
                created = -1
            )
        }
    }

    @Test
    fun testVerificationSessionToStringRedactsSecret() {
        val session = IdentityVerificationSession(
            id = "vs_123",
            clientSecret = "vs_123_secret_abc",
            status = VerificationSessionStatus.REQUIRES_INPUT,
            type = VerificationType.DOCUMENT,
            created = 1234567890
        )

        val string = session.toString()
        assertTrue(string.contains("REDACTED"))
        assertTrue(!string.contains("vs_123_secret_abc"))
    }
}

class VerificationSessionStatusTest {
    @Test
    fun testFromString() {
        assertEquals(VerificationSessionStatus.REQUIRES_INPUT, VerificationSessionStatus.fromString("requires_input"))
        assertEquals(VerificationSessionStatus.PROCESSING, VerificationSessionStatus.fromString("processing"))
        assertEquals(VerificationSessionStatus.VERIFIED, VerificationSessionStatus.fromString("verified"))
        assertEquals(VerificationSessionStatus.CANCELED, VerificationSessionStatus.fromString("canceled"))
    }

    @Test
    fun testFromStringCaseInsensitive() {
        assertEquals(VerificationSessionStatus.REQUIRES_INPUT, VerificationSessionStatus.fromString("REQUIRES_INPUT"))
        assertEquals(VerificationSessionStatus.PROCESSING, VerificationSessionStatus.fromString("Processing"))
    }

    @Test
    fun testFromStringUnknown() {
        assertNull(VerificationSessionStatus.fromString("unknown"))
    }
}

class VerificationTypeTest {
    @Test
    fun testFromString() {
        assertEquals(VerificationType.DOCUMENT, VerificationType.fromString("document"))
        assertEquals(VerificationType.ID_NUMBER, VerificationType.fromString("id_number"))
        assertEquals(VerificationType.ADDRESS, VerificationType.fromString("address"))
    }

    @Test
    fun testFromStringUnknown() {
        assertNull(VerificationType.fromString("unknown"))
    }
}

class VerificationErrorCodeTest {
    @Test
    fun testFromString() {
        assertEquals(VerificationErrorCode.ABANDONED, VerificationErrorCode.fromString("abandoned"))
        assertEquals(VerificationErrorCode.CONSENT_DECLINED, VerificationErrorCode.fromString("consent_declined"))
        assertEquals(VerificationErrorCode.DOCUMENT_EXPIRED, VerificationErrorCode.fromString("document_expired"))
        assertEquals(VerificationErrorCode.SELFIE_FACE_MISMATCH, VerificationErrorCode.fromString("selfie_face_mismatch"))
    }

    @Test
    fun testFromStringUnknown() {
        assertEquals(VerificationErrorCode.UNKNOWN, VerificationErrorCode.fromString("unknown_error"))
    }

    @Test
    fun testAllErrorCodes() {
        // Test that all error codes can be parsed
        val codes = listOf(
            "abandoned", "consent_declined", "country_not_supported",
            "device_not_supported", "document_country_not_supported",
            "document_expired", "document_type_not_supported",
            "document_unverified_other", "email_unverified_other",
            "email_verification_declined", "id_number_insufficient_document_data",
            "id_number_mismatch", "id_number_unverified_other",
            "phone_unverified_other", "phone_verification_declined",
            "selfie_document_missing_photo", "selfie_face_mismatch",
            "selfie_manipulated", "selfie_unverified_other",
            "under_supported_age"
        )

        codes.forEach { code ->
            val errorCode = VerificationErrorCode.fromString(code)
            assertNotNull(errorCode, "Failed to parse: $code")
            assertTrue(errorCode != VerificationErrorCode.UNKNOWN, "Unexpected UNKNOWN for: $code")
        }
    }
}

class VerificationSessionErrorTest {
    @Test
    fun testErrorCreation() {
        val error = VerificationSessionError(
            code = VerificationErrorCode.DOCUMENT_EXPIRED,
            reason = "The provided document has expired"
        )

        assertEquals(VerificationErrorCode.DOCUMENT_EXPIRED, error.code)
        assertEquals("The provided document has expired", error.reason)
    }

    @Test
    fun testErrorWithoutReason() {
        val error = VerificationSessionError(
            code = VerificationErrorCode.ABANDONED
        )

        assertEquals(VerificationErrorCode.ABANDONED, error.code)
        assertNull(error.reason)
    }
}

class VerifiedOutputsTest {
    @Test
    fun testVerifiedOutputsCreation() {
        val address = Address(
            line1 = "123 Main St",
            city = "San Francisco",
            state = "CA",
            postalCode = "94102",
            country = "US"
        )

        val dateOfBirth = DateOfBirth(day = 15, month = 6, year = 1990)

        val outputs = VerifiedOutputs(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = dateOfBirth,
            address = address,
            idNumber = "123-45-6789",
            idNumberType = IdNumberType.US_SSN
        )

        assertEquals("John", outputs.firstName)
        assertEquals("Doe", outputs.lastName)
        assertEquals(dateOfBirth, outputs.dateOfBirth)
        assertEquals(address, outputs.address)
        assertEquals(IdNumberType.US_SSN, outputs.idNumberType)
    }

    @Test
    fun testVerifiedOutputsToStringRedactsIdNumber() {
        val outputs = VerifiedOutputs(
            firstName = "John",
            lastName = "Doe",
            idNumber = "123-45-6789",
            idNumberType = IdNumberType.US_SSN
        )

        val string = outputs.toString()
        assertTrue(string.contains("REDACTED"))
        assertTrue(!string.contains("123-45-6789"))
    }

    @Test
    fun testVerifiedOutputsWithNullFields() {
        val outputs = VerifiedOutputs()

        assertNull(outputs.firstName)
        assertNull(outputs.lastName)
        assertNull(outputs.dateOfBirth)
        assertNull(outputs.address)
        assertNull(outputs.idNumber)
        assertNull(outputs.idNumberType)
    }
}

class DateOfBirthTest {
    @Test
    fun testDateOfBirthCreation() {
        val dob = DateOfBirth(day = 15, month = 6, year = 1990)

        assertEquals(15, dob.day)
        assertEquals(6, dob.month)
        assertEquals(1990, dob.year)
    }

    @Test
    fun testDateOfBirthDayValidation() {
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 0, month = 6, year = 1990)
        }
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 32, month = 6, year = 1990)
        }
    }

    @Test
    fun testDateOfBirthMonthValidation() {
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 15, month = 0, year = 1990)
        }
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 15, month = 13, year = 1990)
        }
    }

    @Test
    fun testDateOfBirthYearValidation() {
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 15, month = 6, year = 1899)
        }
        assertFailsWith<IllegalArgumentException> {
            DateOfBirth(day = 15, month = 6, year = 2101)
        }
    }

    @Test
    fun testDateOfBirthBoundaryValues() {
        // Test valid boundary values
        DateOfBirth(day = 1, month = 1, year = 1900)
        DateOfBirth(day = 31, month = 12, year = 2100)
    }
}

class IdNumberTypeTest {
    @Test
    fun testFromString() {
        assertEquals(IdNumberType.BR_CPF, IdNumberType.fromString("br_cpf"))
        assertEquals(IdNumberType.SG_NRIC, IdNumberType.fromString("sg_nric"))
        assertEquals(IdNumberType.US_SSN, IdNumberType.fromString("us_ssn"))
    }

    @Test
    fun testFromStringUnknown() {
        assertNull(IdNumberType.fromString("unknown"))
    }
}

class IdentityVerificationSheetConfigurationTest {
    @Test
    fun testConfigurationCreation() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_123",
            ephemeralKeySecret = "ek_test_123",
            brandLogo = "https://example.com/logo.png"
        )

        assertEquals("vs_123", config.verificationSessionId)
        assertEquals("https://example.com/logo.png", config.brandLogo)
    }

    @Test
    fun testConfigurationWithoutBrandLogo() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_123",
            ephemeralKeySecret = "ek_test_123"
        )

        assertNull(config.brandLogo)
    }

    @Test
    fun testConfigurationSessionIdValidation() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSheetConfiguration(
                verificationSessionId = "invalid",
                ephemeralKeySecret = "ek_test_123"
            )
        }
    }

    @Test
    fun testConfigurationEphemeralKeyValidation() {
        assertFailsWith<IllegalArgumentException> {
            IdentityVerificationSheetConfiguration(
                verificationSessionId = "vs_123",
                ephemeralKeySecret = "invalid"
            )
        }
    }

    @Test
    fun testConfigurationToStringRedactsSecret() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_123",
            ephemeralKeySecret = "ek_test_123"
        )

        val string = config.toString()
        assertTrue(string.contains("REDACTED"))
        assertTrue(!string.contains("ek_test_123"))
    }
}

class IdentityVerificationSheetResultTest {
    @Test
    fun testCompletedResult() {
        val session = IdentityVerificationSession(
            id = "vs_123",
            clientSecret = "vs_123_secret_abc",
            status = VerificationSessionStatus.VERIFIED,
            type = VerificationType.DOCUMENT,
            created = 1234567890
        )

        val result = IdentityVerificationSheetResult.Completed(session)
        assertEquals(session, result.session)
    }

    @Test
    fun testCanceledResult() {
        val result = IdentityVerificationSheetResult.Canceled
        assertNotNull(result)
    }

    @Test
    fun testFailedResult() {
        val exception = StripeException(
            message = "Verification failed",
            statusCode = 400
        )

        val result = IdentityVerificationSheetResult.Failed(exception)
        assertEquals(exception, result.error)
        assertEquals("Verification failed", result.error.message)
    }

    @Test
    fun testResultIsSealed() {
        // Test that we can use when expression exhaustively
        val result: IdentityVerificationSheetResult = IdentityVerificationSheetResult.Canceled

        val handled = when (result) {
            is IdentityVerificationSheetResult.Completed -> "completed"
            is IdentityVerificationSheetResult.Canceled -> "canceled"
            is IdentityVerificationSheetResult.Failed -> "failed"
        }

        assertEquals("canceled", handled)
    }
}
