package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * Platform-agnostic payment authenticator for handling 3D Secure and other authentication flows.
 *
 * This class provides methods to authenticate payments and setup intents, handling required
 * actions like 3D Secure challenges, redirects, and other Strong Customer Authentication (SCA)
 * requirements.
 *
 * Platform-specific implementations:
 * - **Android**: Uses `STPPaymentHandler` and Stripe 3DS2 SDK
 * - **iOS**: Uses `STPPaymentHandler` with `STPAuthenticationContext`
 * - **JavaScript**: Uses `stripe.handleNextAction()` and `stripe.handleCardAction()`
 * - **WASM**: Returns unsupported error
 *
 * Usage:
 * ```kotlin
 * val authenticator = PaymentAuthenticator.getInstance()
 *
 * // Authenticate a payment (mobile)
 * val result = authenticator.authenticatePayment(activity, clientSecret)
 * when (result) {
 *     is AuthenticationResult.Completed -> // Payment authenticated
 *     is AuthenticationResult.Canceled -> // User canceled
 *     is AuthenticationResult.Failed -> // Authentication failed
 * }
 *
 * // Handle next action (web)
 * val result = authenticator.handleNextAction(clientSecret)
 * ```
 */
public expect class PaymentAuthenticator {
    public companion object {
        /**
         * Get the singleton instance of PaymentAuthenticator.
         *
         * @return The PaymentAuthenticator instance
         */
        public fun getInstance(): PaymentAuthenticator
    }

    /**
     * Handle next action for a payment intent.
     * This method automatically determines what action is required and handles it.
     *
     * This is the web-friendly version that doesn't require platform-specific context.
     * For mobile platforms, prefer using [handleNextActionForPayment] with activity/controller.
     *
     * @param clientSecret The client secret of the PaymentIntent
     * @return Authentication result with the updated PaymentIntent
     */
    public suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult

    /**
     * Handle next action for a payment intent with platform context.
     *
     * On Android, the activity parameter should be a ComponentActivity.
     * On iOS, it should be a UIViewController.
     * On JS/WASM, the activity parameter is ignored.
     *
     * @param activity Platform-specific activity/view controller for presenting authentication UI
     * @param clientSecret The client secret of the PaymentIntent
     * @return Authentication result with the updated PaymentIntent
     */
    public suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult

    /**
     * Handle next action for a setup intent with platform context.
     *
     * On Android, the activity parameter should be a ComponentActivity.
     * On iOS, it should be a UIViewController.
     * On JS/WASM, the activity parameter is ignored.
     *
     * @param activity Platform-specific activity/view controller for presenting authentication UI
     * @param clientSecret The client secret of the SetupIntent
     * @return Authentication result with the updated SetupIntent
     */
    public suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult

    /**
     * Authenticate a payment intent end-to-end.
     * This is a convenience method that handles confirmation and authentication in one call.
     *
     * Note: This requires the payment intent to already have a payment method attached,
     * or you should use the Stripe.confirmPaymentIntent() method instead.
     *
     * @param activity Platform-specific activity/view controller for presenting authentication UI
     * @param clientSecret The client secret of the PaymentIntent
     * @return Authentication result with the final PaymentIntent
     */
    public suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult

    /**
     * Authenticate a setup intent end-to-end.
     * This is a convenience method that handles confirmation and authentication in one call.
     *
     * Note: This requires the setup intent to already have a payment method attached,
     * or you should use the Stripe.confirmSetupIntent() method instead.
     *
     * @param activity Platform-specific activity/view controller for presenting authentication UI
     * @param clientSecret The client secret of the SetupIntent
     * @return Authentication result with the final SetupIntent
     */
    public suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult

    /**
     * Handle 3DS2 challenge specifically.
     * This is used when you have explicit 3DS2 challenge data and need to present it.
     *
     * Most applications should use [handleNextAction] instead, which automatically
     * determines the authentication type. This method is for advanced use cases where
     * you're manually managing the 3DS flow.
     *
     * @param activity Platform-specific activity/view controller for presenting challenge UI
     * @param challenge The 3DS2 challenge data
     * @return Response from the 3DS2 authentication
     */
    public suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse
}
