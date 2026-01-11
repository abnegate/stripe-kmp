package com.jakebarnby.stripe

/**
 * Configuration for the Payment Sheet UI.
 *
 * @property merchantDisplayName The name of your business that will be displayed to customers
 * @property customerId Optional customer ID for saving payment methods
 * @property customerEphemeralKeySecret Optional ephemeral key for customer operations
 * @property allowsDelayedPaymentMethods Allow payment methods that require delayed confirmation (default: true)
 * @throws IllegalArgumentException if merchantDisplayName is blank
 * @throws IllegalArgumentException if only one of customerId or customerEphemeralKeySecret is provided
 */
public data class PaymentSheetConfiguration(
    val merchantDisplayName: String,
    val customerId: String? = null,
    val customerEphemeralKeySecret: String? = null,
    val allowsDelayedPaymentMethods: Boolean = true
) {
    init {
        // HIGH-01: Validate merchant name
        require(merchantDisplayName.isNotBlank()) {
            "Merchant display name cannot be blank"
        }

        // HIGH-04: Validate customer configuration consistency
        val hasCustomerId = customerId != null
        val hasEphemeralKey = customerEphemeralKeySecret != null
        require(hasCustomerId == hasEphemeralKey) {
            "Both customerId and customerEphemeralKeySecret must be provided together, or both must be null. " +
            "Received customerId=${if (hasCustomerId) "present" else "null"}, " +
            "customerEphemeralKeySecret=${if (hasEphemeralKey) "present" else "null"}"
        }
    }

    /**
     * Override toString to prevent ephemeral key from being exposed in logs.
     */
    override fun toString(): String {
        return "PaymentSheetConfiguration(" +
            "merchantDisplayName='$merchantDisplayName', " +
            "customerId=${if (customerId != null) "'$customerId'" else "null"}, " +
            "customerEphemeralKeySecret=${if (customerEphemeralKeySecret != null) "***REDACTED***" else "null"}, " +
            "allowsDelayedPaymentMethods=$allowsDelayedPaymentMethods)"
    }
}

/**
 * Configuration for presenting a Payment Sheet with a PaymentIntent.
 *
 * @property clientSecret The PaymentIntent client secret from your server
 * @property paymentSheetConfiguration Additional configuration for the payment sheet
 * @throws IllegalArgumentException if clientSecret doesn't match PaymentIntent format
 */
public data class PaymentIntentConfiguration(
    val clientSecret: String,
    val paymentSheetConfiguration: PaymentSheetConfiguration
) {
    init {
        // HIGH-05: Validate client secret format for PaymentIntent
        require(clientSecret.matches(Regex("^pi_[a-zA-Z0-9]+_secret_[a-zA-Z0-9]+$"))) {
            "Invalid PaymentIntent client secret format. Expected format: 'pi_xxx_secret_xxx'."
        }
    }
}

/**
 * Configuration for presenting a Payment Sheet with a SetupIntent.
 *
 * @property clientSecret The SetupIntent client secret from your server
 * @property paymentSheetConfiguration Additional configuration for the payment sheet
 * @throws IllegalArgumentException if clientSecret doesn't match SetupIntent format
 */
public data class SetupIntentConfiguration(
    val clientSecret: String,
    val paymentSheetConfiguration: PaymentSheetConfiguration
) {
    init {
        // HIGH-05: Validate client secret format for SetupIntent
        require(clientSecret.matches(Regex("^seti_[a-zA-Z0-9]+_secret_[a-zA-Z0-9]+$"))) {
            "Invalid SetupIntent client secret format. Expected format: 'seti_xxx_secret_xxx'."
        }
    }
}

/**
 * Result of presenting the Payment Sheet to the user.
 */
public sealed class PaymentSheetResult {
    /**
     * The customer successfully completed the payment.
     */
    public data object Completed : PaymentSheetResult()

    /**
     * The customer canceled the payment.
     */
    public data object Canceled : PaymentSheetResult()

    /**
     * The payment failed with an error.
     */
    public data class Failed(val error: StripeError) : PaymentSheetResult()
}

/**
 * Represents a Stripe error with message and code.
 *
 * @property message The error message describing what went wrong
 * @property code Optional error code for programmatic error handling
 */
public data class StripeError(
    val message: String,
    val code: String? = null
)

/**
 * Platform-specific Payment Sheet interface.
 * This provides the main payment UI for accepting payments.
 */
public expect class PaymentSheet {
    /**
     * Present the Payment Sheet with a PaymentIntent.
     * This will show the native payment UI to the customer.
     *
     * @param configuration The PaymentIntent configuration
     * @param onResult Callback invoked with the payment result
     */
    public suspend fun presentWithPaymentIntent(
        configuration: PaymentIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    )

    /**
     * Present the Payment Sheet with a SetupIntent.
     * This will show the native payment UI for saving payment methods.
     *
     * @param configuration The SetupIntent configuration
     * @param onResult Callback invoked with the result
     */
    public suspend fun presentWithSetupIntent(
        configuration: SetupIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    )
}
