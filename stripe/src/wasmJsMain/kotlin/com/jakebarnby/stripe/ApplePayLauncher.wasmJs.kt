package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WebAssembly implementation of ApplePayLauncher.
 *
 * Apple Pay is not currently supported in WebAssembly environments.
 * All methods return appropriate error results or false for availability checks.
 */
public actual class ApplePayLauncher {

    public actual suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not supported in WebAssembly environment")
        )
    }

    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not supported in WebAssembly environment")
        )
    }

    public actual suspend fun createPaymentMethod(
        configuration: ApplePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not supported in WebAssembly environment")
        )
    }

    public actual companion object {
        public actual fun isAvailable(): Boolean = false

        public actual fun canMakePayments(): Boolean = false

        public actual fun canMakePaymentsWithNetworks(networks: List<CardBrand>): Boolean = false
    }
}
