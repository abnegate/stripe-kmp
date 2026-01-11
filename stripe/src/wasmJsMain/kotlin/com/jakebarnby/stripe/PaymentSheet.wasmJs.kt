package com.jakebarnby.stripe

/**
 * WASM implementation of PaymentSheet.
 *
 * CRITICAL-03: WASM support is intentionally limited.
 *
 * The WASM target does not currently have proper JS interop for Stripe.js.
 * This implementation immediately fails to make it clear that WASM is not supported.
 *
 * For web applications, use the JS target instead.
 * For native applications, use Android or iOS targets.
 *
 * If you need WASM support, you must implement custom JS interop bridges.
 */
public actual class PaymentSheet {
    /**
     * WASM PaymentSheet is not implemented.
     *
     * This will immediately fail with a clear error message.
     * Use the JS, Android, or iOS targets for production applications.
     */
    public actual suspend fun presentWithPaymentIntent(
        configuration: PaymentIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        onResult(
            PaymentSheetResult.Failed(
                StripeError(
                    message = "WASM target is not supported for Stripe PaymentSheet. " +
                            "Please use JS target for web, or Android/iOS for native platforms.",
                    code = "platform_not_supported"
                )
            )
        )
    }

    /**
     * WASM PaymentSheet is not implemented.
     *
     * This will immediately fail with a clear error message.
     * Use the JS, Android, or iOS targets for production applications.
     */
    public actual suspend fun presentWithSetupIntent(
        configuration: SetupIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        onResult(
            PaymentSheetResult.Failed(
                StripeError(
                    message = "WASM target is not supported for Stripe PaymentSheet. " +
                            "Please use JS target for web, or Android/iOS for native platforms.",
                    code = "platform_not_supported"
                )
            )
        )
    }
}
