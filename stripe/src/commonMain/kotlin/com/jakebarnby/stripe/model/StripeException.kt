package com.jakebarnby.stripe.model

/**
 * Base exception for all Stripe-related errors.
 *
 * @property message Human-readable error message
 * @property stripeError Optional Stripe error details
 * @property statusCode Optional HTTP status code
 * @property requestId Optional Stripe request ID for debugging
 * @property cause Optional underlying cause
 */
public open class StripeException(
    override val message: String,
    public val stripeError: StripeError? = null,
    public val statusCode: Int? = null,
    public val requestId: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when card processing fails.
 * Typically includes decline reasons and suggested user actions.
 */
public class CardException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null,
    public val declineCode: String? = null,
    public val charge: String? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Exception thrown when request parameters are invalid.
 * Check the error message and stripeError for details on which parameters are invalid.
 */
public class InvalidRequestException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null,
    public val param: String? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Exception thrown when API authentication fails.
 * Check your publishable or secret key.
 */
public class AuthenticationException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Exception thrown when network communication with Stripe fails.
 * This could be due to network connectivity issues or timeouts.
 */
public class APIConnectionException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Exception thrown when the API key lacks necessary permissions.
 */
public class PermissionException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Exception thrown when too many requests are made in a short period.
 * Implement exponential backoff when handling this error.
 */
public class RateLimitException(
    message: String,
    stripeError: StripeError? = null,
    statusCode: Int? = null,
    requestId: String? = null,
    cause: Throwable? = null
) : StripeException(message, stripeError, statusCode, requestId, cause)

/**
 * Detailed error information from Stripe API.
 *
 * @property type Error type (e.g., "card_error", "invalid_request_error")
 * @property code Error code for programmatic handling
 * @property message Human-readable error message
 * @property param Parameter that caused the error
 * @property declineCode Card decline code if applicable
 * @property charge Charge ID if applicable
 */
public data class StripeError(
    val type: String,
    val code: String? = null,
    val message: String,
    val param: String? = null,
    val declineCode: String? = null,
    val charge: String? = null
)

/**
 * Comprehensive list of Stripe error codes for programmatic error handling.
 * These codes help you handle specific error scenarios gracefully.
 */
public enum class StripeErrorCode(public val code: String) {
    // Card errors
    CARD_DECLINED("card_declined"),
    EXPIRED_CARD("expired_card"),
    INCORRECT_CVC("incorrect_cvc"),
    INCORRECT_NUMBER("incorrect_number"),
    INCORRECT_ZIP("incorrect_zip"),
    INVALID_CVC("invalid_cvc"),
    INVALID_EXPIRY_MONTH("invalid_expiry_month"),
    INVALID_EXPIRY_YEAR("invalid_expiry_year"),
    INVALID_NUMBER("invalid_number"),
    PROCESSING_ERROR("processing_error"),

    // Decline codes
    INSUFFICIENT_FUNDS("insufficient_funds"),
    LOST_CARD("lost_card"),
    STOLEN_CARD("stolen_card"),
    FRAUDULENT("fraudulent"),
    DO_NOT_HONOR("do_not_honor"),
    DO_NOT_TRY_AGAIN("do_not_try_again"),
    GENERIC_DECLINE("generic_decline"),

    // Request errors
    INVALID_REQUEST_ERROR("invalid_request_error"),
    MISSING("missing"),
    PARAMETER_INVALID_EMPTY("parameter_invalid_empty"),
    PARAMETER_INVALID_INTEGER("parameter_invalid_integer"),
    PARAMETER_INVALID_STRING_BLANK("parameter_invalid_string_blank"),
    PARAMETER_INVALID_STRING_EMPTY("parameter_invalid_string_empty"),
    PARAMETER_MISSING("parameter_missing"),
    PARAMETER_UNKNOWN("parameter_unknown"),

    // Authentication errors
    API_KEY_EXPIRED("api_key_expired"),
    AUTHENTICATION_REQUIRED("authentication_required"),

    // Rate limit errors
    RATE_LIMIT("rate_limit"),

    // Payment errors
    AMOUNT_TOO_LARGE("amount_too_large"),
    AMOUNT_TOO_SMALL("amount_too_small"),
    BALANCE_INSUFFICIENT("balance_insufficient"),
    CHARGE_ALREADY_CAPTURED("charge_already_captured"),
    CHARGE_ALREADY_REFUNDED("charge_already_refunded"),
    CHARGE_DISPUTED("charge_disputed"),
    CHARGE_EXCEEDS_SOURCE_LIMIT("charge_exceeds_source_limit"),
    CHARGE_EXPIRED_FOR_CAPTURE("charge_expired_for_capture"),
    COUNTRY_UNSUPPORTED("country_unsupported"),
    CURRENCY_NOT_SUPPORTED("currency_not_supported"),
    EMAIL_INVALID("email_invalid"),
    EXPIRED_CARD_TOKEN("expired_card_token"),
    INSTANT_PAYOUTS_UNSUPPORTED("instant_payouts_unsupported"),
    INVALID_CARD_TYPE("invalid_card_type"),
    INVALID_CHARGE_AMOUNT("invalid_charge_amount"),
    INVALID_SOURCE_USAGE("invalid_source_usage"),
    PAYMENT_INTENT_AUTHENTICATION_FAILURE("payment_intent_authentication_failure"),
    PAYMENT_INTENT_INCOMPATIBLE_PAYMENT_METHOD("payment_intent_incompatible_payment_method"),
    PAYMENT_INTENT_INVALID_PARAMETER("payment_intent_invalid_parameter"),
    PAYMENT_INTENT_PAYMENT_ATTEMPT_FAILED("payment_intent_payment_attempt_failed"),
    PAYMENT_INTENT_UNEXPECTED_STATE("payment_intent_unexpected_state"),
    PAYMENT_METHOD_UNACTIVATED("payment_method_unactivated"),
    PAYMENT_METHOD_UNEXPECTED_STATE("payment_method_unexpected_state"),
    PAYOUTS_NOT_ALLOWED("payouts_not_allowed"),
    RESOURCE_MISSING("resource_missing"),
    SECRET_KEY_REQUIRED("secret_key_required"),
    SEPA_UNSUPPORTED_ACCOUNT("sepa_unsupported_account"),
    SETUP_INTENT_AUTHENTICATION_FAILURE("setup_intent_authentication_failure"),
    SETUP_INTENT_INVALID_PARAMETER("setup_intent_invalid_parameter"),
    SETUP_INTENT_SETUP_ATTEMPT_FAILED("setup_intent_setup_attempt_failed"),
    SETUP_INTENT_UNEXPECTED_STATE("setup_intent_unexpected_state"),
    TOKEN_ALREADY_USED("token_already_used"),
    TOKEN_IN_USE("token_in_use"),
    TRANSFERS_NOT_ALLOWED("transfers_not_allowed"),

    // Unknown/other
    UNKNOWN("unknown");

    public companion object {
        /**
         * Find error code by string value.
         * Returns UNKNOWN if code is not recognized.
         */
        public fun fromCode(code: String): StripeErrorCode {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
