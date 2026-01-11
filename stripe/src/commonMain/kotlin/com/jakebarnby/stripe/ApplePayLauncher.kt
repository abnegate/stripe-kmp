package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.ApplePayConfiguration
import com.jakebarnby.stripe.model.CardBrand
import com.jakebarnby.stripe.model.WalletPaymentRequest
import com.jakebarnby.stripe.model.WalletPaymentResult

/**
 * Apple Pay launcher for processing payments through Apple Pay.
 *
 * Platform-specific implementations handle the native Apple Pay integration
 * on iOS, and use Payment Request API on web platforms. On Android and other
 * platforms, the launcher returns unsupported errors.
 *
 * Example usage:
 * ```kotlin
 * // Check availability
 * if (ApplePayLauncher.isAvailable()) {
 *     val launcher = ApplePayLauncher()
 *
 *     val config = ApplePayConfiguration(
 *         merchantIdentifier = "merchant.com.example",
 *         merchantCountryCode = "US",
 *         currencyCode = "USD"
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
public expect class ApplePayLauncher() {
    /**
     * Present Apple Pay for a PaymentIntent.
     *
     * This will show the Apple Pay payment sheet to the user and automatically
     * confirm the PaymentIntent upon successful payment.
     *
     * @param clientSecret The PaymentIntent client secret
     * @param configuration Apple Pay configuration
     * @return Result of the payment operation
     */
    public suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult

    /**
     * Present Apple Pay for a SetupIntent.
     *
     * This will show the Apple Pay payment sheet to the user and automatically
     * confirm the SetupIntent to save the payment method for future use.
     *
     * @param clientSecret The SetupIntent client secret
     * @param configuration Apple Pay configuration
     * @return Result of the setup operation
     */
    public suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult

    /**
     * Create a PaymentMethod using Apple Pay without confirming a payment.
     *
     * This is useful when you want to collect payment information but confirm
     * the payment later or on your server.
     *
     * @param configuration Apple Pay configuration
     * @param request Payment request details (amount, currency, etc.)
     * @return Result containing the created PaymentMethod
     */
    public suspend fun createPaymentMethod(
        configuration: ApplePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult

    public companion object {
        /**
         * Check if Apple Pay is available on this device/platform.
         *
         * On iOS, this checks if the device supports Apple Pay hardware and
         * the user has configured a card. On web platforms, this checks if
         * the Payment Request API is available and supports Apple Pay.
         *
         * @return true if Apple Pay is available, false otherwise
         */
        public fun isAvailable(): Boolean

        /**
         * Check if the user can make payments.
         *
         * This is a more lenient check than isAvailable() - it returns true
         * if the device supports Apple Pay hardware, even if the user hasn't
         * added any cards yet.
         *
         * @return true if the device can make payments, false otherwise
         */
        public fun canMakePayments(): Boolean

        /**
         * Check if the user can make payments with specific card networks.
         *
         * This checks if the user has at least one card configured that matches
         * one of the specified networks.
         *
         * @param networks List of card networks to check
         * @return true if the user can make payments with at least one of the networks
         */
        public fun canMakePaymentsWithNetworks(networks: List<CardBrand>): Boolean
    }
}
