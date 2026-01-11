package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WASM implementation of IdentityVerificationSheet.
 *
 * WASM support is intentionally limited. The WASM target does not currently have
 * proper JS interop for Stripe Identity verification flows.
 *
 * This implementation immediately returns a Failed result to make it clear that
 * WASM is not supported for Identity Verification.
 *
 * ## Recommendations:
 *
 * - For web applications, use the **JS target** instead
 * - For native mobile applications, use **Android** or **iOS** targets
 * - WASM is primarily designed for computational workloads, not UI-heavy operations
 *
 * If you absolutely need WASM support, you must implement custom JS interop bridges
 * to communicate with Stripe's JavaScript SDK, which is a complex undertaking.
 */
public actual class IdentityVerificationSheet {
    /**
     * WASM IdentityVerificationSheet is not implemented.
     *
     * This will immediately return a Failed result with a clear error message.
     * Use the JS, Android, or iOS targets for production applications.
     *
     * @param configuration The verification session configuration
     * @return Failed result indicating platform is not supported
     */
    public actual suspend fun present(
        configuration: IdentityVerificationSheetConfiguration
    ): IdentityVerificationSheetResult {
        return IdentityVerificationSheetResult.Failed(
            StripeException(
                message = "WASM target is not supported for Stripe Identity Verification. " +
                        "Please use JS target for web applications, or Android/iOS for native mobile platforms. " +
                        "If you need WASM support, you must implement custom JS interop bridges."
            )
        )
    }
}
