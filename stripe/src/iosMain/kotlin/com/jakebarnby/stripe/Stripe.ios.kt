package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.atomicfu.atomic
import platform.Foundation.NSLog

/**
 * iOS Stripe implementation - Swift Bridge Pattern
 *
 * This implementation uses documented stubs that direct developers to use the Swift bridge
 * for actual Stripe operations. The StripePaymentSheet CocoaPod does not expose the full
 * Stripe API (STPAPIClient, STPToken, etc.) - those are in the StripePayments pod which
 * requires complex cinterop setup.
 *
 * RECOMMENDED APPROACH:
 * Use the Swift bridge in your iOS app for Stripe operations. See:
 * - iosApp/iosApp/StripeBridge.swift (reference implementation)
 * - stripe/src/iosMain/kotlin/com/jakebarnby/stripe/README_IOS_IMPLEMENTATION.md
 *
 * For PaymentSheet operations, use PaymentSheet.kt which has native cinterop bindings.
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    init {
        if (configuration.enableLogging) {
            NSLog("Stripe KMP initialized for iOS")
            NSLog("For full Stripe API operations, use StripeBridge.swift in your iOS app")
            NSLog("PaymentSheet operations are available via PaymentSheet.kt")
        }
    }

    // ============================================================================
    // Token Creation - Use Swift Bridge
    // ============================================================================

    public actual suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = StripeResult.failure(
        StripeException(
            "iOS card token creation requires Swift bridge. " +
            "Use StripeBridge.shared.createCardToken() from Swift. " +
            "The StripePaymentSheet pod does not expose STPAPIClient. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = StripeResult.failure(
        StripeException(
            "iOS bank account token creation requires Swift bridge. " +
            "Use StripeBridge.shared.createBankAccountToken() from Swift. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun createPiiToken(
        params: PiiTokenParams
    ): StripeResult<Token> = StripeResult.failure(
        StripeException(
            "PII tokens are not supported via iOS client SDK for security reasons. " +
            "Use server-side token creation instead."
        )
    )

    public actual suspend fun createAccountToken(
        params: AccountParams
    ): StripeResult<Token> = StripeResult.failure(
        StripeException(
            "Account tokens (Stripe Connect) are not supported via iOS client SDK for security reasons. " +
            "Use server-side token creation instead."
        )
    )

    // ============================================================================
    // Source Creation - Use Swift Bridge
    // ============================================================================

    public actual suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = StripeResult.failure(
        StripeException(
            "iOS source creation requires Swift bridge. " +
            "Use StripeBridge.shared.createSource() from Swift. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = StripeResult.failure(
        StripeException(
            "iOS source retrieval requires Swift bridge. " +
            "Use StripeBridge.shared.retrieveSource() from Swift. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    // ============================================================================
    // PaymentMethod - Use Swift Bridge
    // ============================================================================

    public actual suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = StripeResult.failure(
        StripeException(
            "iOS payment method creation requires Swift bridge. " +
            "Use StripeBridge.shared.createPaymentMethod() from Swift. " +
            "Alternatively, use PaymentSheet for complete payment flows. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun retrievePaymentMethod(
        paymentMethodId: String
    ): StripeResult<PaymentMethod> = StripeResult.failure(
        StripeException(
            "Payment method retrieval requires server-side implementation for security"
        )
    )

    // ============================================================================
    // PaymentIntent - Use Swift Bridge or PaymentSheet
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent> = StripeResult.failure(
        StripeException(
            "iOS PaymentIntent retrieval requires Swift bridge. " +
            "Use StripeBridge.shared.retrievePaymentIntent() from Swift. " +
            "For complete payment flows, use PaymentSheet instead. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = StripeResult.failure(
        StripeException(
            "iOS PaymentIntent confirmation requires Swift bridge. " +
            "Use StripeBridge.shared.confirmPaymentIntent() from Swift. " +
            "For complete payment flows with UI, use PaymentSheet instead. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun handleNextActionForPayment(
        clientSecret: String
    ): StripeResult<PaymentIntent> = StripeResult.failure(
        StripeException(
            "iOS PaymentIntent next action handling requires UIViewController context. " +
            "Use PaymentAuthenticator.handleNextActionForPayment(viewController, clientSecret) or " +
            "use PaymentSheet for complete payment flows with UI. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    // ============================================================================
    // SetupIntent - Use Swift Bridge or PaymentSheet
    // ============================================================================

    public actual suspend fun retrieveSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = StripeResult.failure(
        StripeException(
            "iOS SetupIntent retrieval requires Swift bridge. " +
            "Use StripeBridge.shared.retrieveSetupIntent() from Swift. " +
            "For complete setup flows, use PaymentSheet instead. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = StripeResult.failure(
        StripeException(
            "iOS SetupIntent confirmation requires Swift bridge. " +
            "Use StripeBridge.shared.confirmSetupIntent() from Swift. " +
            "For complete setup flows with UI, use PaymentSheet instead. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    public actual suspend fun handleNextActionForSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = StripeResult.failure(
        StripeException(
            "iOS SetupIntent next action handling requires UIViewController context. " +
            "Use PaymentAuthenticator.handleNextActionForSetupIntent(viewController, clientSecret) or " +
            "use PaymentSheet for complete setup flows with UI. " +
            "See: iosApp/iosApp/StripeBridge.swift"
        )
    )

    // ============================================================================
    // Customer - Server-side only
    // ============================================================================

    public actual suspend fun retrieveCustomer(
        customerId: String
    ): StripeResult<Customer> = StripeResult.failure(
        StripeException(
            "Customer retrieval requires server-side implementation for security"
        )
    )

    public actual suspend fun createEphemeralKey(
        params: EphemeralKeyCreateParams
    ): StripeResult<EphemeralKey> = StripeResult.failure(
        StripeException(
            "Ephemeral key creation requires server-side implementation for security"
        )
    )

    // ============================================================================
    // Singleton Management
    // ============================================================================

    public actual companion object {
        private val instance = atomic<Stripe?>(null)

        /**
         * Initialize the Stripe SDK with the provided configuration.
         *
         * @param configuration The Stripe configuration
         * @return A configured Stripe instance
         */
        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            val stripe = Stripe(configuration)
            instance.value = stripe
            return stripe
        }

        /**
         * Get the current Stripe instance.
         *
         * @return The current Stripe instance
         * @throws IllegalStateException if Stripe has not been initialized
         */
        public actual fun getInstance(): Stripe {
            return requireNotNull(instance.value) {
                "Stripe has not been initialized. Call Stripe.initialize() first."
            }
        }
    }
}

/**
 * iOS IMPLEMENTATION NOTES:
 *
 * The StripePaymentSheet CocoaPod only exposes PaymentSheet-related classes.
 * For full Stripe API access (tokens, sources, payment methods, etc.), you need:
 *
 * Option A: Add StripePayments pod (Complex)
 *   - Add pod("StripePayments") to build.gradle.kts
 *   - Configure cinterop for StripePayments
 *   - Implement converters for STPAPIClient, STPToken, etc.
 *
 * Option B: Use Swift Bridge (RECOMMENDED)
 *   - Implement Stripe operations in Swift using native SDK
 *   - Call Swift bridge from Kotlin via expect/actual or direct calls
 *   - See iosApp/iosApp/StripeBridge.swift for reference
 *
 * For most use cases, PaymentSheet provides a complete payment UI solution
 * and is available via PaymentSheet.kt with full cinterop support.
 */
