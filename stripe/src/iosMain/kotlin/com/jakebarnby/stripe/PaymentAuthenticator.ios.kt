package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * iOS implementation of PaymentAuthenticator - Swift Bridge Pattern
 *
 * Payment authentication on iOS requires UIViewController context and access to
 * STPPaymentHandler from the StripePayments pod, which is not included in the
 * StripePaymentSheet pod.
 *
 * RECOMMENDED APPROACH:
 * - For complete payment flows with UI: Use PaymentSheet.kt
 * - For custom authentication flows: Use Swift bridge
 * - See iosApp/iosApp/StripeBridge.swift for reference implementation
 */
public actual class PaymentAuthenticator private constructor() {

    public actual companion object {
        @kotlin.concurrent.Volatile
        private var instance: PaymentAuthenticator? = null

        /**
         * Get the singleton instance of PaymentAuthenticator.
         */
        public actual fun getInstance(): PaymentAuthenticator {
            return instance ?: synchronized(this) {
                instance ?: PaymentAuthenticator().also { instance = it }
            }
        }

        private fun synchronized(lock: Any, block: () -> PaymentAuthenticator): PaymentAuthenticator {
            return block()
        }
    }

    /**
     * Handle next action for a payment intent without view controller context.
     *
     * On iOS, authentication flows require a UIViewController for presenting
     * authentication screens. This method returns a failure indicating that
     * the view controller version should be used instead.
     */
    public actual suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "iOS authentication requires UIViewController context. " +
                "Use Swift bridge: StripeBridge.shared.handleNextAction(viewController:clientSecret:) or " +
                "use PaymentSheet for complete payment flows with UI. " +
                "See: iosApp/iosApp/StripeBridge.swift"
            )
        )
    }

    /**
     * Handle next action for a payment intent with UIViewController context.
     *
     * This requires STPPaymentHandler from the StripePayments pod.
     * Use Swift bridge for implementation.
     *
     * @param activity UIViewController for presenting authentication UI (must be UIViewController)
     * @param clientSecret The client secret of the PaymentIntent
     */
    public actual suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "iOS PaymentIntent authentication requires Swift bridge. " +
                "Use StripeBridge.shared.handleNextActionForPayment(viewController:clientSecret:) from Swift. " +
                "Alternatively, use PaymentSheet for complete payment flows with UI. " +
                "The StripePaymentSheet pod does not expose STPPaymentHandler. " +
                "See: iosApp/iosApp/StripeBridge.swift"
            )
        )
    }

    /**
     * Handle next action for a setup intent with UIViewController context.
     *
     * This requires STPPaymentHandler from the StripePayments pod.
     * Use Swift bridge for implementation.
     *
     * @param activity UIViewController for presenting authentication UI (must be UIViewController)
     * @param clientSecret The client secret of the SetupIntent
     */
    public actual suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "iOS SetupIntent authentication requires Swift bridge. " +
                "Use StripeBridge.shared.handleNextActionForSetupIntent(viewController:clientSecret:) from Swift. " +
                "Alternatively, use PaymentSheet for complete setup flows with UI. " +
                "The StripePaymentSheet pod does not expose STPPaymentHandler. " +
                "See: iosApp/iosApp/StripeBridge.swift"
            )
        )
    }

    /**
     * Authenticate a payment intent end-to-end.
     *
     * This confirms the payment and handles any required authentication.
     * Requires Swift bridge implementation.
     *
     * @param activity UIViewController for presenting authentication UI
     * @param clientSecret The client secret of the PaymentIntent
     */
    public actual suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "iOS payment authentication requires Swift bridge. " +
                "Use StripeBridge.shared.authenticatePayment(viewController:clientSecret:) from Swift. " +
                "Alternatively, use PaymentSheet for complete payment flows with UI. " +
                "See: iosApp/iosApp/StripeBridge.swift"
            )
        )
    }

    /**
     * Authenticate a setup intent end-to-end.
     *
     * This confirms the setup and handles any required authentication.
     * Requires Swift bridge implementation.
     *
     * @param activity UIViewController for presenting authentication UI
     * @param clientSecret The client secret of the SetupIntent
     */
    public actual suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "iOS setup intent authentication requires Swift bridge. " +
                "Use StripeBridge.shared.authenticateSetupIntent(viewController:clientSecret:) from Swift. " +
                "Alternatively, use PaymentSheet for complete setup flows with UI. " +
                "See: iosApp/iosApp/StripeBridge.swift"
            )
        )
    }

    /**
     * Handle 3DS2 challenge specifically.
     *
     * On iOS, 3DS2 challenges are handled automatically by STPPaymentHandler
     * when using handleNextAction methods. This requires the Swift bridge.
     *
     * @param activity UIViewController for presenting challenge UI
     * @param challenge The 3DS2 challenge data
     */
    public actual suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse {
        return Stripe3ds2AuthenticationResponse(
            id = challenge.threeDSecureServerTransactionId,
            ares = null,
            error = "iOS handles 3DS2 automatically via STPPaymentHandler. " +
                   "Use Swift bridge: StripeBridge.shared.handleNextActionForPayment() or " +
                   "use PaymentSheet for complete payment flows. " +
                   "See: iosApp/iosApp/StripeBridge.swift",
            fallbackRedirectUrl = challenge.acsUrl,
            state = AuthenticationState.REDIRECT_REQUIRED
        )
    }
}

/**
 * iOS PAYMENT AUTHENTICATION NOTES:
 *
 * The StripePaymentSheet pod does not include STPPaymentHandler or authentication
 * handling classes. For authentication flows on iOS:
 *
 * Option A: Use PaymentSheet (RECOMMENDED for most cases)
 *   - PaymentSheet handles all authentication automatically
 *   - Provides complete payment UI with 3DS, redirects, etc.
 *   - Available via PaymentSheet.kt with full cinterop support
 *
 * Option B: Use Swift Bridge (for custom flows)
 *   - Add pod("StripePayments") to get STPPaymentHandler
 *   - Implement authentication in Swift
 *   - Call from Kotlin via StripeBridge
 *   - See iosApp/iosApp/StripeBridge.swift for reference
 *
 * Option C: Add StripePayments pod and complex cinterop (not recommended)
 *   - Add pod("StripePayments") to build.gradle.kts
 *   - Configure cinterop for StripePayments
 *   - Implement converters for STPPaymentHandler, etc.
 */
