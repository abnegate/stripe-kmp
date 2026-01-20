package com.jakebarnby.stripe

/**
 * JVM implementation of PaymentSheet.
 * PaymentSheet UI is not available on JVM as it's a server-side target.
 */
public actual class PaymentSheet {
    public actual suspend fun presentWithPaymentIntent(
        configuration: PaymentIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        onResult(
            PaymentSheetResult.Failed(
                StripeError("PaymentSheet UI is not available on JVM. Use Android, iOS, or JS target.")
            )
        )
    }

    public actual suspend fun presentWithSetupIntent(
        configuration: SetupIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        onResult(
            PaymentSheetResult.Failed(
                StripeError("PaymentSheet UI is not available on JVM. Use Android, iOS, or JS target.")
            )
        )
    }
}
