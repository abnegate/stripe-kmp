package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.IdentityVerificationSheetConfiguration
import com.jakebarnby.stripe.model.IdentityVerificationSheetResult

/**
 * Identity Verification Sheet for collecting and verifying user identity information.
 *
 * This provides a native UI for identity verification across all platforms.
 * Users can verify their identity by uploading documents, taking selfies,
 * and providing personal information.
 *
 * ## Usage
 *
 * ### Android
 * ```kotlin
 * // Set the current activity
 * setIdentityVerificationSheetActivity(this)
 *
 * // Create configuration
 * val config = IdentityVerificationSheetConfiguration(
 *     verificationSessionId = "vs_xxx",
 *     ephemeralKeySecret = "ek_test_xxx",
 *     brandLogo = "https://your-logo-url.com/logo.png"
 * )
 *
 * // Present the sheet
 * val sheet = IdentityVerificationSheet()
 * sheet.present(config) { result ->
 *     when (result) {
 *         is IdentityVerificationSheetResult.Completed -> {
 *             // Verification flow completed
 *             // Check session.status on your server
 *         }
 *         is IdentityVerificationSheetResult.Canceled -> {
 *             // User canceled
 *         }
 *         is IdentityVerificationSheetResult.Failed -> {
 *             // Handle error
 *         }
 *     }
 * }
 * ```
 *
 * ### iOS
 * ```kotlin
 * val config = IdentityVerificationSheetConfiguration(
 *     verificationSessionId = "vs_xxx",
 *     ephemeralKeySecret = "ek_test_xxx"
 * )
 *
 * val sheet = IdentityVerificationSheet()
 * sheet.present(config) { result ->
 *     // Handle result
 * }
 * ```
 *
 * ### JavaScript
 * ```kotlin
 * // Note: JS implementation redirects to hosted verification URL
 * val config = IdentityVerificationSheetConfiguration(
 *     verificationSessionId = "vs_xxx",
 *     ephemeralKeySecret = "ek_test_xxx"
 * )
 *
 * val sheet = IdentityVerificationSheet()
 * sheet.present(config) { result ->
 *     // Handle result
 * }
 * ```
 *
 * ## Important Notes
 *
 * - The verification flow completion does not guarantee successful verification
 * - Always check the verification session status on your server after completion
 * - Use webhooks to receive real-time updates on verification status changes
 * - The ephemeral key should be generated on your server for security
 */
public expect class IdentityVerificationSheet {
    /**
     * Present the Identity Verification Sheet to the user.
     *
     * This will show the native identity verification UI where users can:
     * - Upload identity documents (passport, driver's license, ID card)
     * - Take a selfie for face matching
     * - Provide personal information
     *
     * @param configuration The verification session configuration
     * @return The result of the verification flow
     */
    public suspend fun present(configuration: IdentityVerificationSheetConfiguration): IdentityVerificationSheetResult
}
