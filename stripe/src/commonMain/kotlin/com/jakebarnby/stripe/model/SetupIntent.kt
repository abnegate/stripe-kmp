package com.jakebarnby.stripe.model

import com.jakebarnby.stripe.SecretString

/**
 * SetupIntent object representing an intent to set up a payment method for future use.
 * SetupIntents guide you through collecting and saving payment method details without an immediate charge.
 *
 * @property id Unique identifier for the SetupIntent
 * @property clientSecret Client secret for confirming the setup. WARNING: This is sensitive data.
 *   Prefer using [getClientSecretSafe] which returns a [SecretString] wrapper.
 * @property created Creation timestamp
 * @property livemode Whether in live mode
 * @property status Current status
 * @property paymentMethodId ID of the payment method being set up
 * @property paymentMethodTypes List of payment method types allowed
 * @property description Description of the setup
 * @property usage How the payment method is intended to be used
 * @property customerId ID of the customer this setup belongs to
 * @property lastSetupError Last error that occurred during setup
 * @property nextAction Next action required to complete the setup
 * @property cancellationReason Reason for cancellation
 * @property metadata Set of key-value pairs
 */
public data class SetupIntent(
    val id: String,
    val clientSecret: String,
    val created: Long,
    val livemode: Boolean,
    val status: SetupIntentStatus,
    val paymentMethodId: String? = null,
    val paymentMethodTypes: List<String> = emptyList(),
    val description: String? = null,
    val usage: SetupIntentUsage = SetupIntentUsage.OFF_SESSION,
    val customerId: String? = null,
    val lastSetupError: SetupIntentError? = null,
    val nextAction: SetupNextAction? = null,
    val cancellationReason: String? = null,
    val metadata: Map<String, String>? = null
) {
    init {
        require(id.isNotBlank()) { "SetupIntent id cannot be blank" }
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
        require(created > 0) { "created timestamp must be positive" }
    }

    /**
     * Get the client secret wrapped in a [SecretString] for safe handling.
     *
     * This method returns the client secret in a wrapper that:
     * - Redacts the value in toString() to prevent accidental logging
     * - Uses constant-time comparison in equals() to prevent timing attacks
     *
     * @return SecretString wrapper containing the client secret
     */
    public fun getClientSecretSafe(): SecretString = SecretString.wrap(clientSecret)

    /**
     * Override toString to redact sensitive clientSecret.
     */
    override fun toString(): String {
        return "SetupIntent(id='$id', clientSecret='***REDACTED***', created=$created, livemode=$livemode, status=$status)"
    }
}

/**
 * Status of a SetupIntent.
 */
public enum class SetupIntentStatus(public val value: String) {
    /** Setup requires a payment method */
    REQUIRES_PAYMENT_METHOD("requires_payment_method"),

    /** Setup requires confirmation */
    REQUIRES_CONFIRMATION("requires_confirmation"),

    /** Setup requires additional action (e.g., 3D Secure) */
    REQUIRES_ACTION("requires_action"),

    /** Setup is processing */
    PROCESSING("processing"),

    /** Setup was canceled */
    CANCELED("canceled"),

    /** Setup succeeded */
    SUCCEEDED("succeeded");

    public companion object {
        public fun fromValue(value: String): SetupIntentStatus? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * How the payment method is intended to be used.
 */
public enum class SetupIntentUsage(public val value: String) {
    /** For on-session payments (customer present) */
    ON_SESSION("on_session"),

    /** For off-session payments (customer not present) */
    OFF_SESSION("off_session");

    public companion object {
        public fun fromValue(value: String): SetupIntentUsage? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Error information from a failed setup attempt.
 *
 * @property type Error type
 * @property code Error code
 * @property declineCode Decline code if applicable
 * @property message Human-readable error message
 * @property paymentMethod Payment method that caused the error
 */
public data class SetupIntentError(
    val type: String,
    val code: String? = null,
    val declineCode: String? = null,
    val message: String,
    val paymentMethod: PaymentMethod? = null
)

/**
 * Next action required to complete the setup.
 *
 * @property type Type of next action required
 * @property redirectToUrl Redirect details if type is REDIRECT_TO_URL
 * @property useStripeSdk SDK-specific data if type is USE_STRIPE_SDK
 */
public data class SetupNextAction(
    val type: SetupNextActionType,
    val redirectToUrl: RedirectToUrl? = null,
    val useStripeSdk: Map<String, Any>? = null
) {
    init {
        when (type) {
            SetupNextActionType.REDIRECT_TO_URL -> require(redirectToUrl != null) {
                "redirectToUrl must be provided when type is REDIRECT_TO_URL"
            }
            SetupNextActionType.USE_STRIPE_SDK -> require(useStripeSdk != null) {
                "useStripeSdk must be provided when type is USE_STRIPE_SDK"
            }
            else -> {}
        }
    }
}

/**
 * Type of next action required for setup.
 */
public enum class SetupNextActionType(public val value: String) {
    /** Redirect to URL for authentication */
    REDIRECT_TO_URL("redirect_to_url"),

    /** Use Stripe SDK for authentication */
    USE_STRIPE_SDK("use_stripe_sdk"),

    /** Verify with microdeposits */
    VERIFY_WITH_MICRODEPOSITS("verify_with_microdeposits");

    public companion object {
        public fun fromValue(value: String): SetupNextActionType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Parameters for confirming a SetupIntent.
 */
public data class ConfirmSetupIntentParams(
    val clientSecret: String,
    val paymentMethodId: String? = null,
    val paymentMethodCreateParams: PaymentMethodCreateParams? = null,
    val returnUrl: String? = null,
    val mandate: String? = null,
    val mandateData: MandateData? = null
) {
    init {
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }

        // Must provide either paymentMethodId or paymentMethodCreateParams
        val hasPaymentMethodId = paymentMethodId != null
        val hasPaymentMethodCreateParams = paymentMethodCreateParams != null

        // At least one should be provided (though not always required - e.g., already attached)
        // The validation depends on the SetupIntent's current state
    }

    /**
     * Builder for ConfirmSetupIntentParams.
     */
    public class Builder(private val clientSecret: String) {
        private var paymentMethodId: String? = null
        private var paymentMethodCreateParams: PaymentMethodCreateParams? = null
        private var returnUrl: String? = null
        private var mandate: String? = null
        private var mandateData: MandateData? = null

        public fun paymentMethodId(paymentMethodId: String?): Builder = apply { this.paymentMethodId = paymentMethodId }
        public fun paymentMethodCreateParams(params: PaymentMethodCreateParams?): Builder = apply { this.paymentMethodCreateParams = params }
        public fun returnUrl(returnUrl: String?): Builder = apply { this.returnUrl = returnUrl }
        public fun mandate(mandate: String?): Builder = apply { this.mandate = mandate }
        public fun mandateData(mandateData: MandateData?): Builder = apply { this.mandateData = mandateData }

        public fun build(): ConfirmSetupIntentParams = ConfirmSetupIntentParams(
            clientSecret = clientSecret,
            paymentMethodId = paymentMethodId,
            paymentMethodCreateParams = paymentMethodCreateParams,
            returnUrl = returnUrl,
            mandate = mandate,
            mandateData = mandateData
        )
    }

    public companion object {
        /**
         * Create parameters with an existing payment method ID.
         */
        public fun createWithPaymentMethodId(
            paymentMethodId: String,
            clientSecret: String
        ): ConfirmSetupIntentParams {
            return ConfirmSetupIntentParams(
                clientSecret = clientSecret,
                paymentMethodId = paymentMethodId
            )
        }

        /**
         * Create parameters with payment method creation parameters.
         */
        public fun createWithPaymentMethodCreateParams(
            paymentMethodCreateParams: PaymentMethodCreateParams,
            clientSecret: String
        ): ConfirmSetupIntentParams {
            return ConfirmSetupIntentParams(
                clientSecret = clientSecret,
                paymentMethodCreateParams = paymentMethodCreateParams
            )
        }

        /**
         * Create a builder for ConfirmSetupIntentParams.
         */
        public fun builder(clientSecret: String): Builder = Builder(clientSecret)
    }
}
