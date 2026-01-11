package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WebAssembly implementation of GooglePayLauncher.
 *
 * Google Pay is not currently supported in WebAssembly environments.
 * All methods return appropriate error results or false for availability checks.
 */
public actual class GooglePayLauncher {

    public actual suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Google Pay is not supported in WebAssembly environment")
        )
    }

    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Google Pay is not supported in WebAssembly environment")
        )
    }

    public actual suspend fun createPaymentMethod(
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Google Pay is not supported in WebAssembly environment")
        )
    }

    public actual companion object {
        public actual fun isAvailable(context: Any?): Boolean = false
    }
}
