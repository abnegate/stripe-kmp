package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WASM implementation of Financial Connections Sheet.
 *
 * CRITICAL: WASM support is intentionally limited.
 *
 * The WASM target does not currently have proper JS interop for Stripe.js Financial Connections.
 * This implementation immediately fails to make it clear that WASM is not supported.
 *
 * For web applications, use the JS target instead.
 * For native applications, use Android or iOS targets.
 *
 * If you need WASM support, you must implement custom JS interop bridges.
 */
public actual class FinancialConnectionsSheet private constructor(
    private val configuration: FinancialConnectionsSheetConfiguration
) {
    public actual companion object {
        /**
         * Create a new Financial Connections Sheet instance.
         *
         * @param configuration The configuration for the sheet
         * @return A new FinancialConnectionsSheet instance
         */
        public actual fun create(
            configuration: FinancialConnectionsSheetConfiguration
        ): FinancialConnectionsSheet {
            return FinancialConnectionsSheet(configuration)
        }
    }

    /**
     * WASM Financial Connections Sheet is not implemented.
     *
     * This will immediately fail with a clear error message.
     * Use the JS, Android, or iOS targets for production applications.
     */
    public actual suspend fun present(): FinancialConnectionsSheetResult {
        return FinancialConnectionsSheetResult.Failed(
            error = StripeException(
                message = "WASM target is not supported for Stripe Financial Connections. " +
                        "Please use JS target for web, or Android/iOS for native platforms.",
                stripeError = StripeError(
                    type = "platform_error",
                    code = "platform_not_supported",
                    message = "WASM target is not supported"
                ),
                statusCode = null,
                requestId = null,
                cause = null
            )
        )
    }

    /**
     * WASM Financial Connections Sheet is not implemented.
     *
     * This will immediately fail with a clear error message.
     * Use the JS, Android, or iOS targets for production applications.
     */
    public actual suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult {
        return FinancialConnectionsSheetForTokenResult.Failed(
            error = StripeException(
                message = "WASM target is not supported for Stripe Financial Connections. " +
                        "Please use JS target for web, or Android/iOS for native platforms.",
                stripeError = StripeError(
                    type = "platform_error",
                    code = "platform_not_supported",
                    message = "WASM target is not supported"
                ),
                statusCode = null,
                requestId = null,
                cause = null
            )
        )
    }
}
