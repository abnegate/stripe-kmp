package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WASM implementation of PaymentAuthenticator.
 *
 * WASM support for Stripe is currently limited as Stripe.js is not yet
 * fully compatible with WebAssembly. This implementation returns appropriate
 * unsupported errors for all authentication methods.
 *
 * For web-based authentication in WASM projects, consider:
 * 1. Using the JS target instead of WASM for Stripe integration
 * 2. Implementing server-side authentication and polling for status
 * 3. Using external JavaScript interop to call Stripe.js from WASM
 *
 * Future versions may add WASM support when Stripe.js provides WASM-compatible APIs.
 */
public actual class PaymentAuthenticator private constructor() {
    public actual companion object {
        private var instance: PaymentAuthenticator? = null

        /**
         * Get the singleton instance of PaymentAuthenticator.
         */
        public actual fun getInstance(): PaymentAuthenticator {
            return instance ?: synchronized(PaymentAuthenticator) {
                instance ?: PaymentAuthenticator().also { instance = it }
            }
        }

        private fun <T> synchronized(lock: Any, block: () -> T): T {
            return block()
        }
    }

    /**
     * Handle next action for a payment intent.
     *
     * WASM support is not available. Returns unsupported error.
     */
    public actual suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "Payment authentication is not supported in WebAssembly (WASM). " +
                "Use the JS target for web-based Stripe integration, or implement " +
                "server-side authentication with status polling."
            )
        )
    }

    /**
     * Handle next action for a payment intent with activity context.
     *
     * WASM support is not available. Returns unsupported error.
     */
    public actual suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "Payment authentication is not supported in WebAssembly (WASM). " +
                "Use the JS target for web-based Stripe integration, or implement " +
                "server-side authentication with status polling."
            )
        )
    }

    /**
     * Handle next action for a setup intent with activity context.
     *
     * WASM support is not available. Returns unsupported error.
     */
    public actual suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "Setup intent authentication is not supported in WebAssembly (WASM). " +
                "Use the JS target for web-based Stripe integration, or implement " +
                "server-side authentication with status polling."
            )
        )
    }

    /**
     * Authenticate a payment intent end-to-end.
     *
     * WASM support is not available. Returns unsupported error.
     */
    public actual suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "Payment authentication is not supported in WebAssembly (WASM). " +
                "Use the JS target for web-based Stripe integration, or implement " +
                "server-side authentication with status polling."
            )
        )
    }

    /**
     * Authenticate a setup intent end-to-end.
     *
     * WASM support is not available. Returns unsupported error.
     */
    public actual suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return AuthenticationResult.Failed(
            error = StripeException(
                "Setup intent authentication is not supported in WebAssembly (WASM). " +
                "Use the JS target for web-based Stripe integration, or implement " +
                "server-side authentication with status polling."
            )
        )
    }

    /**
     * Handle 3DS2 challenge specifically.
     *
     * WASM support is not available. Returns error response.
     */
    public actual suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse {
        return Stripe3ds2AuthenticationResponse(
            id = challenge.threeDSecureServerTransactionId,
            ares = null,
            error = "3DS2 authentication is not supported in WebAssembly (WASM). " +
                   "Use the JS target for web-based Stripe integration.",
            fallbackRedirectUrl = challenge.acsUrl,
            state = AuthenticationState.FAILED
        )
    }
}
