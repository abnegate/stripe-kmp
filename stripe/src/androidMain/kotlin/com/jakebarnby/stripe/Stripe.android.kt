package com.jakebarnby.stripe

/**
 * Android Stripe SDK Implementation
 *
 * THREADING MODEL:
 * This implementation uses `withContext(Dispatchers.IO)` for all blocking SDK operations.
 * The Stripe Android SDK provides synchronous methods (e.g., createCardTokenSynchronous)
 * that are designed to be called off the main thread.
 *
 * Dispatchers.IO is appropriate here because:
 * - It provides an elastic thread pool that grows as needed (up to 64 threads by default)
 * - It's specifically designed for blocking I/O operations
 * - Network I/O from Stripe SDK is handled efficiently
 *
 * Alternative approaches considered:
 * - Using callback-based async APIs with suspendCancellableCoroutine: More complex,
 *   requires managing callback lifecycle, but avoids blocking threads entirely.
 * - For high-throughput scenarios, consider using the callback APIs directly.
 */
import android.content.Context
import com.jakebarnby.stripe.model.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe as AndroidStripe
import com.stripe.android.model.CardParams as AndroidCardParams
import com.stripe.android.model.BankAccountTokenParams as AndroidBankAccountTokenParams
import com.stripe.android.model.ConfirmPaymentIntentParams as AndroidConfirmPaymentIntentParams
import com.stripe.android.model.ConfirmSetupIntentParams as AndroidConfirmSetupIntentParams
import com.stripe.android.model.PaymentIntent as AndroidPaymentIntent
import com.stripe.android.model.PaymentMethod as AndroidPaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams as AndroidPaymentMethodCreateParams
import com.stripe.android.model.SetupIntent as AndroidSetupIntent
import com.stripe.android.model.Source as AndroidSource
import com.stripe.android.model.SourceParams as AndroidSourceParams
import com.stripe.android.model.Token as AndroidToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

// CRITICAL-06: Use AtomicReference for thread-safe global state
private val appContext = AtomicReference<Context?>(null)

/**
 * Initialize the Android context for Stripe.
 * This must be called before using any Stripe functionality on Android.
 *
 * IMPORTANT: This should be called from your Application.onCreate() or
 * Activity.onCreate() before initializing Stripe.
 *
 * @param context The application context
 */
public fun initializeStripeContext(context: Context) {
    appContext.set(context.applicationContext)
}

public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    private val androidStripe: AndroidStripe by lazy {
        val ctx = appContext.get()
        requireNotNull(ctx) { "Stripe context not initialized. Call initializeStripeContext() first." }
        AndroidStripe(ctx, configuration.publishableKey)
    }

    internal fun getAndroidStripe(): AndroidStripe = androidStripe

    // ============================================================================
    // Token Creation
    // ============================================================================

    public actual suspend fun createCardToken(params: CardParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            // Note: Android Stripe SDK synchronous methods don't support custom request options
            // Idempotency keys would require using the async API
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = AndroidCardParams(
                number = params.number,
                expMonth = params.expMonth,
                expYear = params.expYear,
                cvc = params.cvc,
                name = params.name,
                address = params.addressLine1?.let {
                    com.stripe.android.model.Address(
                        line1 = params.addressLine1,
                        line2 = params.addressLine2,
                        city = params.addressCity,
                        state = params.addressState,
                        postalCode = params.addressZip,
                        country = params.addressCountry
                    )
                }
            )

            val token = androidStripe.createCardTokenSynchronous(androidParams)
            requireNotNull(token) { "Failed to create card token" }
            token.toKmpToken()
        }
    }

    public actual suspend fun createBankAccountToken(params: BankAccountTokenParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = AndroidBankAccountTokenParams(
                country = params.country,
                currency = params.currency,
                accountNumber = params.accountNumber,
                routingNumber = params.routingNumber,
                accountHolderName = params.accountHolderName,
                accountHolderType = params.accountHolderType?.let {
                    when (it) {
                        BankAccountTokenParams.AccountHolderType.INDIVIDUAL ->
                            AndroidBankAccountTokenParams.Type.Individual
                        BankAccountTokenParams.AccountHolderType.COMPANY ->
                            AndroidBankAccountTokenParams.Type.Company
                    }
                }
            )

            val token = androidStripe.createBankAccountTokenSynchronous(androidParams)
            requireNotNull(token) { "Failed to create bank account token" }
            token.toKmpToken()
        }
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val token = androidStripe.createPiiTokenSynchronous(params.personalIdNumber)
            requireNotNull(token) { "Failed to create PII token" }
            token.toKmpToken()
        }
    }

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.failure(
            StripeException("Account token creation requires server-side implementation or additional Android SDK setup")
        )
    }

    // ============================================================================
    // Source Creation
    // ============================================================================

    public actual suspend fun createSource(params: SourceParams, idempotencyKey: IdempotencyKey?): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = params.toAndroidSourceParams()
            val source = androidStripe.createSourceSynchronous(androidParams)
            requireNotNull(source) { "Failed to create source" }
            source.toKmpSource()
        }
    }

    public actual suspend fun retrieveSource(sourceId: String, clientSecret: String): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val source = androidStripe.retrieveSourceSynchronous(sourceId, clientSecret)
            requireNotNull(source) { "Failed to retrieve source" }
            source.toKmpSource()
        }
    }

    // ============================================================================
    // PaymentMethod
    // ============================================================================

    public actual suspend fun createPaymentMethod(params: PaymentMethodCreateParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentMethod> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = params.toAndroidPaymentMethodCreateParams()
            val paymentMethod = androidStripe.createPaymentMethodSynchronous(androidParams)
            requireNotNull(paymentMethod) { "Failed to create payment method" }
            paymentMethod.toKmpPaymentMethod()
        }
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> = withContext(Dispatchers.IO) {
        StripeResult.failure(
            StripeException("PaymentMethod retrieval not available in Android SDK synchronous API")
        )
    }

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val paymentIntent = androidStripe.retrievePaymentIntentSynchronous(clientSecret)
            requireNotNull(paymentIntent) { "Failed to retrieve payment intent" }
            paymentIntent.toKmpPaymentIntent()
        }
    }

    public actual suspend fun confirmPaymentIntent(params: ConfirmPaymentIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = params.toAndroidConfirmPaymentIntentParams()
            val paymentIntent = androidStripe.confirmPaymentIntentSynchronous(androidParams)
            requireNotNull(paymentIntent) { "Failed to confirm payment intent" }
            paymentIntent.toKmpPaymentIntent()
        }
    }

    /**
     * Handle next action for a PaymentIntent.
     *
     * IMPORTANT: This method requires Activity integration with the Activity Result API.
     * To use this method, you must:
     * 1. Use PaymentAuthenticator.getInstance().handleNextActionForPayment(activity, clientSecret)
     * 2. Or integrate PaymentLauncher directly in your Activity with proper lifecycle management
     *
     * This method returns an error directing you to use the proper Activity-based approach.
     */
    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> = withContext(Dispatchers.IO) {
        StripeResult.failure(
            StripeException(
                "handleNextActionForPayment requires Activity context for authentication. " +
                "Use PaymentAuthenticator.getInstance().handleNextActionForPayment(activity, clientSecret) instead. " +
                "The Activity must be a ComponentActivity with proper lifecycle integration for the Activity Result API."
            )
        )
    }

    // ============================================================================
    // SetupIntent
    // ============================================================================

    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val setupIntent = androidStripe.retrieveSetupIntentSynchronous(clientSecret)
            requireNotNull(setupIntent) { "Failed to retrieve setup intent" }
            setupIntent.toKmpSetupIntent()
        }
    }

    public actual suspend fun confirmSetupIntent(params: ConfirmSetupIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            if (idempotencyKey != null) {
                android.util.Log.w("Stripe", "Idempotency keys are not supported with synchronous Android SDK methods. Consider using async Stripe operations for idempotency support.")
            }

            val androidParams = params.toAndroidConfirmSetupIntentParams()
            val setupIntent = androidStripe.confirmSetupIntentSynchronous(androidParams)
            requireNotNull(setupIntent) { "Failed to confirm setup intent" }
            setupIntent.toKmpSetupIntent()
        }
    }

    /**
     * Handle next action for a SetupIntent.
     *
     * IMPORTANT: This method requires Activity integration with the Activity Result API.
     * To use this method, you must:
     * 1. Use PaymentAuthenticator.getInstance().handleNextActionForSetupIntent(activity, clientSecret)
     * 2. Or integrate PaymentLauncher directly in your Activity with proper lifecycle management
     *
     * This method returns an error directing you to use the proper Activity-based approach.
     */
    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        StripeResult.failure(
            StripeException(
                "handleNextActionForSetupIntent requires Activity context for authentication. " +
                "Use PaymentAuthenticator.getInstance().handleNextActionForSetupIntent(activity, clientSecret) instead. " +
                "The Activity must be a ComponentActivity with proper lifecycle integration for the Activity Result API."
            )
        )
    }

    // ============================================================================
    // Customer (not exposed in common API - server-side only)
    // ============================================================================

    // Note: retrieveCustomer and createEphemeralKey are server-side operations
    // that require a secret key. They are not part of the common API.
    // Use server-side endpoints or the JVM target with stripe-java for these operations.

    public actual companion object {
        // CRITICAL-06: Use AtomicReference for thread-safe singleton
        private val instance = AtomicReference<Stripe?>(null)

        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            val ctx = appContext.get()
            requireNotNull(ctx) { "Stripe context not initialized. Call initializeStripeContext() first." }

            PaymentConfiguration.init(
                ctx,
                configuration.publishableKey,
                configuration.merchantDisplayName
            )

            val stripe = Stripe(configuration)
            instance.set(stripe)
            return stripe
        }

        public actual fun getInstance(): Stripe {
            return requireNotNull(instance.get()) {
                "Stripe has not been initialized. Call Stripe.initialize() first."
            }
        }
    }
}
