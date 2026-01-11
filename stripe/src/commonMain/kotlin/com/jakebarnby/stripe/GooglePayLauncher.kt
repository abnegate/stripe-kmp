package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.GooglePayConfiguration
import com.jakebarnby.stripe.model.WalletPaymentRequest
import com.jakebarnby.stripe.model.WalletPaymentResult

/**
 * Google Pay launcher for processing payments through Google Pay.
 *
 * Platform-specific implementations handle the native Google Pay integration
 * on Android, and use Payment Request API on web platforms.
 *
 * Example usage:
 * ```kotlin
 * // Check availability
 * if (GooglePayLauncher.isAvailable(context)) {
 *     val launcher = GooglePayLauncher()
 *
 *     val config = GooglePayConfiguration(
 *         environment = GooglePayEnvironment.TEST,
 *         merchantName = "Example Merchant",
 *         merchantCountryCode = "US"
 *     )
 *
 *     // For PaymentIntent
 *     val result = launcher.presentForPaymentIntent(
 *         clientSecret = "pi_xxx_secret_xxx",
 *         configuration = config
 *     )
 *
 *     when (result) {
 *         is WalletPaymentResult.Success -> {
 *             // Payment succeeded
 *             println("Payment method: ${result.paymentMethodId}")
 *         }
 *         is WalletPaymentResult.Canceled -> {
 *             // User canceled
 *         }
 *         is WalletPaymentResult.Failed -> {
 *             // Handle error
 *             println("Error: ${result.error.message}")
 *         }
 *     }
 * }
 * ```
 */
public expect class GooglePayLauncher() {
    /**
     * Present Google Pay for a PaymentIntent.
     *
     * This will show the Google Pay payment sheet to the user and automatically
     * confirm the PaymentIntent upon successful payment.
     *
     * @param clientSecret The PaymentIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the payment operation
     */
    public suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult

    /**
     * Present Google Pay for a SetupIntent.
     *
     * This will show the Google Pay payment sheet to the user and automatically
     * confirm the SetupIntent to save the payment method for future use.
     *
     * @param clientSecret The SetupIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the setup operation
     */
    public suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult

    /**
     * Create a PaymentMethod using Google Pay without confirming a payment.
     *
     * This is useful when you want to collect payment information but confirm
     * the payment later or on your server.
     *
     * @param configuration Google Pay configuration
     * @param request Payment request details (amount, currency, etc.)
     * @return Result containing the created PaymentMethod
     */
    public suspend fun createPaymentMethod(
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult

    public companion object {
        /**
         * Check if Google Pay is available on this device/platform.
         *
         * On Android, this checks if the device has Google Play Services and
         * Google Pay installed. On web platforms, this checks if the Payment
         * Request API is available.
         *
         * @param context Platform-specific context (Activity on Android, unused on other platforms)
         * @return true if Google Pay is available, false otherwise
         */
        public fun isAvailable(context: Any? = null): Boolean
    }
}
