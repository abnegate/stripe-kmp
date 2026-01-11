package com.jakebarnby.stripe.model

/**
 * Represents an identity verification session.
 *
 * @property id Unique identifier for the verification session
 * @property clientSecret Client secret for authenticating the session
 * @property status Current status of the verification session
 * @property type Type of verification being performed
 * @property lastError Last error that occurred during verification, if any
 * @property verifiedOutputs Verified data extracted from the verification, if completed
 * @property livemode Whether this is a live mode verification
 * @property created Timestamp when the session was created (Unix timestamp)
 * @property lastVerificationReport ID of the last verification report
 * @property metadata Set of key-value pairs for storing additional information
 */
public data class IdentityVerificationSession(
    val id: String,
    val clientSecret: String,
    val status: VerificationSessionStatus,
    val type: VerificationType,
    val lastError: VerificationSessionError? = null,
    val verifiedOutputs: VerifiedOutputs? = null,
    val livemode: Boolean = false,
    val created: Long,
    val lastVerificationReport: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(id.startsWith("vs_")) {
            "Invalid verification session ID format. Expected format: 'vs_xxx', got: $id"
        }
        require(clientSecret.matches(Regex("^vs_[a-zA-Z0-9_]+_secret_[a-zA-Z0-9_]+$"))) {
            "Invalid verification session client secret format. Expected format: 'vs_xxx_secret_xxx', got: $clientSecret"
        }
        require(created > 0) {
            "Created timestamp must be positive, got: $created"
        }
    }

    /**
     * Override toString to prevent client secret from being exposed in logs.
     */
    override fun toString(): String {
        return "IdentityVerificationSession(" +
            "id='$id', " +
            "clientSecret=***REDACTED***, " +
            "status=$status, " +
            "type=$type, " +
            "lastError=$lastError, " +
            "verifiedOutputs=$verifiedOutputs, " +
            "livemode=$livemode, " +
            "created=$created, " +
            "lastVerificationReport=$lastVerificationReport, " +
            "metadata=$metadata)"
    }
}

/**
 * Status of a verification session.
 */
public enum class VerificationSessionStatus {
    /**
     * The verification session requires user input.
     */
    REQUIRES_INPUT,

    /**
     * The verification session is being processed.
     */
    PROCESSING,

    /**
     * The verification has been successfully verified.
     */
    VERIFIED,

    /**
     * The verification session was canceled.
     */
    CANCELED;

    public companion object {
        /**
         * Parse status from string value.
         * Returns null if the value is not recognized.
         */
        public fun fromString(value: String): VerificationSessionStatus? {
            return when (value.lowercase()) {
                "requires_input" -> REQUIRES_INPUT
                "processing" -> PROCESSING
                "verified" -> VERIFIED
                "canceled" -> CANCELED
                else -> null
            }
        }
    }
}

/**
 * Type of verification being performed.
 */
public enum class VerificationType {
    /**
     * Document verification (ID card, passport, driver's license).
     */
    DOCUMENT,

    /**
     * ID number verification (SSN, CPF, NRIC).
     */
    ID_NUMBER,

    /**
     * Address verification.
     */
    ADDRESS;

    public companion object {
        /**
         * Parse type from string value.
         * Returns null if the value is not recognized.
         */
        public fun fromString(value: String): VerificationType? {
            return when (value.lowercase()) {
                "document" -> DOCUMENT
                "id_number" -> ID_NUMBER
                "address" -> ADDRESS
                else -> null
            }
        }
    }
}

/**
 * Error that occurred during verification.
 *
 * @property code Error code identifying the type of error
 * @property reason Human-readable explanation of the error
 */
public data class VerificationSessionError(
    val code: VerificationErrorCode,
    val reason: String? = null
)

/**
 * Comprehensive list of verification error codes.
 */
public enum class VerificationErrorCode {
    /**
     * The verification was abandoned by the user.
     */
    ABANDONED,

    /**
     * The user declined consent.
     */
    CONSENT_DECLINED,

    /**
     * The country is not supported for verification.
     */
    COUNTRY_NOT_SUPPORTED,

    /**
     * The device is not supported for verification.
     */
    DEVICE_NOT_SUPPORTED,

    /**
     * The document's country is not supported.
     */
    DOCUMENT_COUNTRY_NOT_SUPPORTED,

    /**
     * The document has expired.
     */
    DOCUMENT_EXPIRED,

    /**
     * The document type is not supported.
     */
    DOCUMENT_TYPE_NOT_SUPPORTED,

    /**
     * The document could not be verified for other reasons.
     */
    DOCUMENT_UNVERIFIED_OTHER,

    /**
     * The email could not be verified for other reasons.
     */
    EMAIL_UNVERIFIED_OTHER,

    /**
     * Email verification was declined.
     */
    EMAIL_VERIFICATION_DECLINED,

    /**
     * Insufficient data on the document to verify ID number.
     */
    ID_NUMBER_INSUFFICIENT_DOCUMENT_DATA,

    /**
     * The ID number does not match the document.
     */
    ID_NUMBER_MISMATCH,

    /**
     * The ID number could not be verified for other reasons.
     */
    ID_NUMBER_UNVERIFIED_OTHER,

    /**
     * The phone number could not be verified for other reasons.
     */
    PHONE_UNVERIFIED_OTHER,

    /**
     * Phone verification was declined.
     */
    PHONE_VERIFICATION_DECLINED,

    /**
     * The document is missing a photo for selfie comparison.
     */
    SELFIE_DOCUMENT_MISSING_PHOTO,

    /**
     * The selfie does not match the document photo.
     */
    SELFIE_FACE_MISMATCH,

    /**
     * The selfie appears to have been manipulated.
     */
    SELFIE_MANIPULATED,

    /**
     * The selfie could not be verified for other reasons.
     */
    SELFIE_UNVERIFIED_OTHER,

    /**
     * The user is under the supported age.
     */
    UNDER_SUPPORTED_AGE,

    /**
     * Unknown error code.
     */
    UNKNOWN;

    public companion object {
        /**
         * Parse error code from string value.
         * Returns UNKNOWN if the value is not recognized.
         */
        public fun fromString(value: String): VerificationErrorCode {
            return when (value.lowercase()) {
                "abandoned" -> ABANDONED
                "consent_declined" -> CONSENT_DECLINED
                "country_not_supported" -> COUNTRY_NOT_SUPPORTED
                "device_not_supported" -> DEVICE_NOT_SUPPORTED
                "document_country_not_supported" -> DOCUMENT_COUNTRY_NOT_SUPPORTED
                "document_expired" -> DOCUMENT_EXPIRED
                "document_type_not_supported" -> DOCUMENT_TYPE_NOT_SUPPORTED
                "document_unverified_other" -> DOCUMENT_UNVERIFIED_OTHER
                "email_unverified_other" -> EMAIL_UNVERIFIED_OTHER
                "email_verification_declined" -> EMAIL_VERIFICATION_DECLINED
                "id_number_insufficient_document_data" -> ID_NUMBER_INSUFFICIENT_DOCUMENT_DATA
                "id_number_mismatch" -> ID_NUMBER_MISMATCH
                "id_number_unverified_other" -> ID_NUMBER_UNVERIFIED_OTHER
                "phone_unverified_other" -> PHONE_UNVERIFIED_OTHER
                "phone_verification_declined" -> PHONE_VERIFICATION_DECLINED
                "selfie_document_missing_photo" -> SELFIE_DOCUMENT_MISSING_PHOTO
                "selfie_face_mismatch" -> SELFIE_FACE_MISMATCH
                "selfie_manipulated" -> SELFIE_MANIPULATED
                "selfie_unverified_other" -> SELFIE_UNVERIFIED_OTHER
                "under_supported_age" -> UNDER_SUPPORTED_AGE
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Verified outputs extracted from successful verification.
 *
 * @property firstName Verified first name
 * @property lastName Verified last name
 * @property dateOfBirth Verified date of birth
 * @property address Verified address
 * @property idNumber Verified ID number
 * @property idNumberType Type of ID number
 */
public data class VerifiedOutputs(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: DateOfBirth? = null,
    val address: Address? = null,
    val idNumber: String? = null,
    val idNumberType: IdNumberType? = null
) {
    /**
     * Override toString to prevent ID number from being exposed in logs.
     */
    override fun toString(): String {
        return "VerifiedOutputs(" +
            "firstName=$firstName, " +
            "lastName=$lastName, " +
            "dateOfBirth=$dateOfBirth, " +
            "address=$address, " +
            "idNumber=${if (idNumber != null) "***REDACTED***" else "null"}, " +
            "idNumberType=$idNumberType)"
    }
}

/**
 * Date of birth.
 *
 * @property day Day of the month (1-31)
 * @property month Month (1-12)
 * @property year Year (4 digits)
 */
public data class DateOfBirth(
    val day: Int,
    val month: Int,
    val year: Int
) {
    init {
        require(day in 1..31) {
            "Day must be between 1 and 31, got: $day"
        }
        require(month in 1..12) {
            "Month must be between 1 and 12, got: $month"
        }
        require(year in 1900..2100) {
            "Year must be between 1900 and 2100, got: $year"
        }
    }
}

/**
 * Type of ID number.
 */
public enum class IdNumberType {
    /**
     * Brazilian CPF number.
     */
    BR_CPF,

    /**
     * Singapore NRIC number.
     */
    SG_NRIC,

    /**
     * US Social Security Number.
     */
    US_SSN;

    public companion object {
        /**
         * Parse ID number type from string value.
         * Returns null if the value is not recognized.
         */
        public fun fromString(value: String): IdNumberType? {
            return when (value.lowercase()) {
                "br_cpf" -> BR_CPF
                "sg_nric" -> SG_NRIC
                "us_ssn" -> US_SSN
                else -> null
            }
        }
    }
}

/**
 * Configuration for the Identity Verification Sheet.
 *
 * @property verificationSessionId The verification session ID
 * @property ephemeralKeySecret The ephemeral key secret for authenticating the session
 * @property brandLogo Optional brand logo URL or resource identifier
 * @throws IllegalArgumentException if verificationSessionId format is invalid
 * @throws IllegalArgumentException if ephemeralKeySecret format is invalid
 */
public data class IdentityVerificationSheetConfiguration(
    val verificationSessionId: String,
    val ephemeralKeySecret: String,
    val brandLogo: String? = null
) {
    init {
        require(verificationSessionId.startsWith("vs_")) {
            "Invalid verification session ID format. Expected format: 'vs_xxx', got: $verificationSessionId"
        }
        require(ephemeralKeySecret.startsWith("ek_")) {
            "Invalid ephemeral key secret format. Expected format: 'ek_xxx', got: $ephemeralKeySecret"
        }
    }

    /**
     * Override toString to prevent ephemeral key from being exposed in logs.
     */
    override fun toString(): String {
        return "IdentityVerificationSheetConfiguration(" +
            "verificationSessionId='$verificationSessionId', " +
            "ephemeralKeySecret=***REDACTED***, " +
            "brandLogo=$brandLogo)"
    }
}

/**
 * Result of presenting the Identity Verification Sheet.
 */
public sealed class IdentityVerificationSheetResult {
    /**
     * The verification flow was completed.
     * Note: This does not mean the verification was successful.
     * Check the session status on your server to determine the verification outcome.
     *
     * @property session The verification session with current status
     */
    public data class Completed(val session: IdentityVerificationSession) : IdentityVerificationSheetResult()

    /**
     * The user canceled the verification flow.
     */
    public data object Canceled : IdentityVerificationSheetResult()

    /**
     * The verification flow failed with an error.
     *
     * @property error The error that occurred
     */
    public data class Failed(val error: StripeException) : IdentityVerificationSheetResult()
}
