package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * Android implementation of ApplePayLauncher.
 *
 * Apple Pay is not available on Android devices. All methods return
 * appropriate error results or false for availability checks.
 */
public actual class ApplePayLauncher {

    public actual suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not available on Android")
        )
    }

    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not available on Android")
        )
    }

    public actual suspend fun createPaymentMethod(
        configuration: ApplePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("Apple Pay is not available on Android")
        )
    }

    public actual companion object {
        public actual fun isAvailable(): Boolean = false

        public actual fun canMakePayments(): Boolean = false

        public actual fun canMakePaymentsWithNetworks(networks: List<CardBrand>): Boolean = false
    }
}
