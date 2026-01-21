package com.jakebarnby.stripe.model

import com.jakebarnby.stripe.SecretString

/**
 * PaymentIntent object representing an intent to collect payment.
 * PaymentIntents guide you through the process of collecting payment from your customer.
 *
 * @property id Unique identifier for the PaymentIntent
 * @property clientSecret Client secret for confirming the payment. WARNING: This is sensitive data.
 *   Prefer using [getClientSecretSafe] which returns a [SecretString] wrapper.
 * @property amount Amount intended to be collected
 * @property currency Three-letter ISO currency code
 * @property status Current status
 * @property created Creation timestamp
 * @property livemode Whether in live mode
 * @property paymentMethodId ID of the payment method used
 * @property paymentMethodTypes List of payment method types allowed
 * @property confirmationMethod How the PaymentIntent should be confirmed
 * @property captureMethod How funds should be captured
 * @property description Description of the payment
 * @property receiptEmail Email to send receipt to
 * @property setupFutureUsage Whether to set up the payment method for future use
 * @property lastPaymentError Last error that occurred during payment
 * @property nextAction Next action required to complete the payment
 * @property canceledAt Timestamp when canceled
 * @property cancellationReason Reason for cancellation
 * @property metadata Set of key-value pairs
 */
public data class PaymentIntent(
    val id: String,
    val clientSecret: String,
    val amount: Long,
    val currency: String,
    val status: PaymentIntentStatus,
    val created: Long,
    val livemode: Boolean,
    val paymentMethodId: String? = null,
    val paymentMethodTypes: List<String> = emptyList(),
    val confirmationMethod: ConfirmationMethod = ConfirmationMethod.AUTOMATIC,
    val captureMethod: CaptureMethod = CaptureMethod.AUTOMATIC,
    val description: String? = null,
    val receiptEmail: String? = null,
    val setupFutureUsage: SetupFutureUsage? = null,
    val lastPaymentError: PaymentIntentError? = null,
    val nextAction: NextAction? = null,
    val canceledAt: Long? = null,
    val cancellationReason: String? = null,
    val metadata: Map<String, String>? = null
) {
    init {
        require(id.isNotBlank()) { "PaymentIntent id cannot be blank" }
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
        require(amount >= 0) { "amount must be non-negative" }
        require(currency.length == 3) { "currency must be a three-letter ISO code" }
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
        return "PaymentIntent(id='$id', clientSecret='***REDACTED***', amount=$amount, currency='$currency', status=$status, created=$created, livemode=$livemode)"
    }
}

/**
 * Status of a PaymentIntent.
 */
public enum class PaymentIntentStatus(public val value: String) {
    /** Payment requires a payment method */
    REQUIRES_PAYMENT_METHOD("requires_payment_method"),

    /** Payment requires confirmation */
    REQUIRES_CONFIRMATION("requires_confirmation"),

    /** Payment requires additional action (e.g., 3D Secure) */
    REQUIRES_ACTION("requires_action"),

    /** Payment is processing */
    PROCESSING("processing"),

    /** Payment requires manual capture */
    REQUIRES_CAPTURE("requires_capture"),

    /** Payment was canceled */
    CANCELED("canceled"),

    /** Payment succeeded */
    SUCCEEDED("succeeded");

    public companion object {
        public fun fromValue(value: String): PaymentIntentStatus? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * How the PaymentIntent should be confirmed.
 */
public enum class ConfirmationMethod(public val value: String) {
    /** Confirm automatically when payment method is attached */
    AUTOMATIC("automatic"),

    /** Require explicit confirmation */
    MANUAL("manual");

    public companion object {
        public fun fromValue(value: String): ConfirmationMethod? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * How funds should be captured.
 */
public enum class CaptureMethod(public val value: String) {
    /** Capture funds automatically when authorized */
    AUTOMATIC("automatic"),

    /** Require manual capture */
    MANUAL("manual");

    public companion object {
        public fun fromValue(value: String): CaptureMethod? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Whether to set up the payment method for future use.
 */
public enum class SetupFutureUsage(public val value: String) {
    /** Save for on-session use (customer present) */
    ON_SESSION("on_session"),

    /** Save for off-session use (customer not present) */
    OFF_SESSION("off_session");

    public companion object {
        public fun fromValue(value: String): SetupFutureUsage? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Error information from a failed payment attempt.
 *
 * @property type Error type
 * @property code Error code
 * @property declineCode Decline code if applicable
 * @property message Human-readable error message
 * @property paymentMethod Payment method that caused the error
 */
public data class PaymentIntentError(
    val type: String,
    val code: String? = null,
    val declineCode: String? = null,
    val message: String,
    val paymentMethod: PaymentMethod? = null
)

/**
 * Next action required to complete the payment.
 *
 * @property type Type of next action required
 * @property redirectToUrl Redirect details if type is REDIRECT_TO_URL
 * @property useStripeSdk SDK-specific data if type is USE_STRIPE_SDK
 */
public data class NextAction(
    val type: NextActionType,
    val redirectToUrl: RedirectToUrl? = null,
    val useStripeSdk: Map<String, Any>? = null
) {
    init {
        when (type) {
            NextActionType.REDIRECT_TO_URL -> require(redirectToUrl != null) {
                "redirectToUrl must be provided when type is REDIRECT_TO_URL"
            }
            NextActionType.USE_STRIPE_SDK -> require(useStripeSdk != null) {
                "useStripeSdk must be provided when type is USE_STRIPE_SDK"
            }
            else -> {}
        }
    }
}

/**
 * Type of next action required.
 */
public enum class NextActionType(public val value: String) {
    /** Redirect to URL for authentication */
    REDIRECT_TO_URL("redirect_to_url"),

    /** Use Stripe SDK for authentication */
    USE_STRIPE_SDK("use_stripe_sdk"),

    /** Display OXXO details */
    DISPLAY_OXXO_DETAILS("display_oxxo_details"),

    /** Display Boleto details */
    DISPLAY_BOLETO_DETAILS("display_boleto_details"),

    /** Display Konbini details */
    DISPLAY_KONBINI_DETAILS("display_konbini_details"),

    /** Verify with microdeposits */
    VERIFY_WITH_MICRODEPOSITS("verify_with_microdeposits"),

    /** Alipay handle redirect */
    ALIPAY_HANDLE_REDIRECT("alipay_handle_redirect"),

    /** WeChat Pay display QR code */
    WECHAT_PAY_DISPLAY_QR_CODE("wechat_pay_display_qr_code");

    public companion object {
        public fun fromValue(value: String): NextActionType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Redirect information for authentication.
 *
 * @property url URL to redirect the customer to
 * @property returnUrl URL to redirect back to after authentication
 */
public data class RedirectToUrl(
    val url: String,
    val returnUrl: String?
) {
    init {
        require(url.isNotBlank()) { "url cannot be blank" }
    }
}

/**
 * Parameters for confirming a PaymentIntent.
 */
public data class ConfirmPaymentIntentParams(
    val clientSecret: String,
    val paymentMethodId: String? = null,
    val paymentMethodCreateParams: PaymentMethodCreateParams? = null,
    val returnUrl: String? = null,
    val shipping: ShippingDetails? = null,
    val receiptEmail: String? = null,
    val setupFutureUsage: SetupFutureUsage? = null,
    val mandate: String? = null,
    val mandateData: MandateData? = null,
    val savePaymentMethod: Boolean = false
) {
    init {
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }

        // Must provide either paymentMethodId or paymentMethodCreateParams
        val hasPaymentMethodId = paymentMethodId != null
        val hasPaymentMethodCreateParams = paymentMethodCreateParams != null

        // At least one should be provided (though not always required - e.g., already attached)
        // The validation depends on the PaymentIntent's current state
    }

    /**
     * Builder for ConfirmPaymentIntentParams.
     */
    public class Builder(private val clientSecret: String) {
        private var paymentMethodId: String? = null
        private var paymentMethodCreateParams: PaymentMethodCreateParams? = null
        private var returnUrl: String? = null
        private var shipping: ShippingDetails? = null
        private var receiptEmail: String? = null
        private var setupFutureUsage: SetupFutureUsage? = null
        private var mandate: String? = null
        private var mandateData: MandateData? = null
        private var savePaymentMethod: Boolean = false

        public fun paymentMethodId(paymentMethodId: String?): Builder = apply { this.paymentMethodId = paymentMethodId }
        public fun paymentMethodCreateParams(params: PaymentMethodCreateParams?): Builder = apply { this.paymentMethodCreateParams = params }
        public fun returnUrl(returnUrl: String?): Builder = apply { this.returnUrl = returnUrl }
        public fun shipping(shipping: ShippingDetails?): Builder = apply { this.shipping = shipping }
        public fun receiptEmail(receiptEmail: String?): Builder = apply { this.receiptEmail = receiptEmail }
        public fun setupFutureUsage(setupFutureUsage: SetupFutureUsage?): Builder = apply { this.setupFutureUsage = setupFutureUsage }
        public fun mandate(mandate: String?): Builder = apply { this.mandate = mandate }
        public fun mandateData(mandateData: MandateData?): Builder = apply { this.mandateData = mandateData }
        public fun savePaymentMethod(savePaymentMethod: Boolean): Builder = apply { this.savePaymentMethod = savePaymentMethod }

        public fun build(): ConfirmPaymentIntentParams = ConfirmPaymentIntentParams(
            clientSecret = clientSecret,
            paymentMethodId = paymentMethodId,
            paymentMethodCreateParams = paymentMethodCreateParams,
            returnUrl = returnUrl,
            shipping = shipping,
            receiptEmail = receiptEmail,
            setupFutureUsage = setupFutureUsage,
            mandate = mandate,
            mandateData = mandateData,
            savePaymentMethod = savePaymentMethod
        )
    }

    public companion object {
        /**
         * Create parameters with an existing payment method ID.
         */
        public fun createWithPaymentMethodId(
            paymentMethodId: String,
            clientSecret: String,
            returnUrl: String? = null,
            shipping: ShippingDetails? = null,
            receiptEmail: String? = null,
            setupFutureUsage: SetupFutureUsage? = null
        ): ConfirmPaymentIntentParams {
            return ConfirmPaymentIntentParams(
                clientSecret = clientSecret,
                paymentMethodId = paymentMethodId,
                returnUrl = returnUrl,
                shipping = shipping,
                receiptEmail = receiptEmail,
                setupFutureUsage = setupFutureUsage
            )
        }

        /**
         * Create parameters with payment method creation parameters.
         */
        public fun createWithPaymentMethodCreateParams(
            paymentMethodCreateParams: PaymentMethodCreateParams,
            clientSecret: String,
            returnUrl: String? = null,
            shipping: ShippingDetails? = null,
            receiptEmail: String? = null,
            setupFutureUsage: SetupFutureUsage? = null
        ): ConfirmPaymentIntentParams {
            return ConfirmPaymentIntentParams(
                clientSecret = clientSecret,
                paymentMethodCreateParams = paymentMethodCreateParams,
                returnUrl = returnUrl,
                shipping = shipping,
                receiptEmail = receiptEmail,
                setupFutureUsage = setupFutureUsage
            )
        }

        /**
         * Create a builder for ConfirmPaymentIntentParams.
         */
        public fun builder(clientSecret: String): Builder = Builder(clientSecret)
    }
}

/**
 * Shipping details for a payment.
 *
 * @property name Recipient name
 * @property address Shipping address
 * @property carrier Shipping carrier
 * @property phone Recipient phone
 * @property trackingNumber Tracking number
 */
public data class ShippingDetails(
    val name: String,
    val address: Address,
    val carrier: String? = null,
    val phone: String? = null,
    val trackingNumber: String? = null
) {
    init {
        require(name.isNotBlank()) { "name cannot be blank" }
    }
}

/**
 * Mandate data for payment methods that require a mandate.
 *
 * @property customerAcceptance Information about customer acceptance
 */
public data class MandateData(
    val customerAcceptance: CustomerAcceptance
)

/**
 * Information about how the customer accepted the mandate.
 *
 * @property type Type of acceptance (online or offline)
 * @property acceptedAt Timestamp when accepted
 * @property online Online acceptance details
 */
public data class CustomerAcceptance(
    val type: String,
    val acceptedAt: Long,
    val online: OnlineAcceptance? = null
) {
    init {
        require(type in listOf("online", "offline")) { "type must be 'online' or 'offline'" }
        require(acceptedAt > 0) { "acceptedAt must be positive" }
    }
}

/**
 * Online acceptance details.
 *
 * @property ipAddress Customer's IP address
 * @property userAgent Customer's user agent
 */
public data class OnlineAcceptance(
    val ipAddress: String,
    val userAgent: String
) {
    init {
        require(ipAddress.isNotBlank()) { "ipAddress cannot be blank" }
        require(userAgent.isNotBlank()) { "userAgent cannot be blank" }
    }
}
