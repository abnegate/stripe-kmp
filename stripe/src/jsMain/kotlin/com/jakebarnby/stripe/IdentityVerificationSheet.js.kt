package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * JavaScript/Browser implementation of IdentityVerificationSheet.
 *
 * Unlike Android and iOS, the JavaScript implementation of Identity Verification
 * does not use an embedded SDK. Instead, it redirects the user to a Stripe-hosted
 * verification page.
 *
 * ## How it works:
 *
 * 1. Create a VerificationSession on your server:
 *    ```
 *    POST https://api.stripe.com/v1/identity/verification_sessions
 *    ```
 *
 * 2. The API returns a `url` property that points to the hosted verification page
 *
 * 3. This implementation redirects the user to that URL
 *
 * 4. After verification, Stripe redirects back to your `return_url`
 *
 * 5. Check the verification status via webhook or by fetching the session from your server
 *
 * ## Usage:
 *
 * ```kotlin
 * // Get the verification session URL from your server
 * val verificationSession = fetchVerificationSessionFromServer()
 *
 * // Present will redirect to the hosted verification page
 * val sheet = IdentityVerificationSheet()
 * val result = sheet.present(
 *     IdentityVerificationSheetConfiguration(
 *         verificationSessionId = verificationSession.id,
 *         ephemeralKeySecret = verificationSession.ephemeralKeySecret
 *     )
 * )
 *
 * // Note: The result will be Failed with a redirect message
 * // The actual verification happens on the hosted page
 * // Your return_url should handle the redirect back
 * ```
 *
 * ## Important Notes:
 *
 * - The browser will navigate away from your page
 * - Set up a `return_url` when creating the VerificationSession on your server
 * - Use webhooks (`identity.verification_session.verified`, `identity.verification_session.requires_input`)
 *   to receive real-time updates on verification status
 * - When the user returns to your return_url, check the session status on your server
 *
 * ## Alternative Implementation:
 *
 * If you need to stay within your app, you can:
 * 1. Open the verification URL in an iframe (may be blocked by Stripe's CSP)
 * 2. Open the URL in a popup window
 * 3. Use a webview component if available
 *
 * Example with popup:
 * ```kotlin
 * val popup = window.open(
 *     verificationSession.url,
 *     "identity_verification",
 *     "width=800,height=600"
 * )
 * // Listen for the popup to close or for a message from the return_url
 * ```
 */
public actual class IdentityVerificationSheet {
    /**
     * Present the Identity Verification flow.
     *
     * For JavaScript/browser environments, this will redirect the user to Stripe's
     * hosted verification page. You must create the VerificationSession on your server
     * first and configure a `return_url`.
     *
     * This method immediately returns Failed with a message explaining that the
     * verification requires server-side setup. To actually redirect, you need to:
     *
     * 1. Create a VerificationSession on your server
     * 2. Get the `url` property from the session
     * 3. Redirect using: `window.location.href = verificationSession.url`
     *
     * @param configuration The verification session configuration
     * @return Result indicating that server-side setup is required
     */
    public actual suspend fun present(
        configuration: IdentityVerificationSheetConfiguration
    ): IdentityVerificationSheetResult {
        return suspendCancellableCoroutine { continuation ->
            // In JavaScript, identity verification is hosted by Stripe
            // We need to redirect to the verification URL

            // Create a helpful error message explaining the process
            val result = IdentityVerificationSheetResult.Failed(
                StripeException(
                    message = """
                        JavaScript Identity Verification requires redirecting to a Stripe-hosted page.

                        To implement identity verification in JavaScript:

                        1. Create a VerificationSession on your server:
                           POST /v1/identity/verification_sessions

                        2. The response includes a 'url' property

                        3. Redirect the user to this URL:
                           window.location.href = verificationSession.url

                        4. After verification, Stripe redirects to your return_url

                        5. Check verification status via webhook or API

                        Verification Session ID: ${configuration.verificationSessionId}

                        For a seamless experience, fetch the verification session from your
                        server and redirect to the URL manually.
                    """.trimIndent()
                )
            )

            // Resume immediately with the explanatory error
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
    }

    public companion object {
        /**
         * Helper method to redirect to a verification URL.
         *
         * Call this after creating a VerificationSession on your server.
         *
         * @param verificationUrl The URL from the VerificationSession object
         */
        public fun redirectToVerification(verificationUrl: String) {
            window.location.href = verificationUrl
        }

        /**
         * Helper method to open verification in a popup window.
         *
         * This allows the user to complete verification without leaving your page.
         * You'll need to communicate with the popup via postMessage or by detecting
         * when it closes.
         *
         * @param verificationUrl The URL from the VerificationSession object
         * @param windowFeatures Optional window features (default: centered popup)
         * @return The popup window reference
         */
        public fun openVerificationPopup(
            verificationUrl: String,
            windowFeatures: String = "width=800,height=600,left=100,top=100"
        ): dynamic {
            return window.open(
                verificationUrl,
                "stripe_identity_verification",
                windowFeatures
            )
        }
    }
}
