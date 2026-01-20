package com.jakebarnby.stripe

/**
 * Android Stripe SDK Implementation using shared REST API client.
 *
 * Headless operations (tokens, payment methods, intents) use the REST API via Ktor.
 * UI components (PaymentSheet, Google Pay) use the native Stripe Android SDK.
 *
 * NOTE: The Stripe Android SDK is still included as a dependency for UI components
 * like PaymentSheet, GooglePayLauncher, and PaymentAuthenticator.
 */
import android.content.Context
import com.jakebarnby.stripe.model.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe as AndroidStripe
import java.util.concurrent.atomic.AtomicReference

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
    private val apiClient = StripeApiClient(configuration, createHttpClientEngine())

    private val androidStripe: AndroidStripe by lazy {
        val ctx = appContext.get()
        requireNotNull(ctx) { "Stripe context not initialized. Call initializeStripeContext() first." }
        AndroidStripe(ctx, configuration.publishableKey)
    }

    /**
     * Get the native Android Stripe SDK instance for UI components.
     * This is used internally by PaymentSheet, GooglePayLauncher, etc.
     */
    internal fun getAndroidStripe(): AndroidStripe = androidStripe


    public actual suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = apiClient.createCardToken(params, idempotencyKey)

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = apiClient.createBankAccountToken(params, idempotencyKey)

    public actual suspend fun createPiiToken(
        params: PiiTokenParams
    ): StripeResult<Token> = apiClient.createPiiToken(params)

    public actual suspend fun createAccountToken(
        params: AccountParams
    ): StripeResult<Token> = apiClient.createAccountToken(params)


    public actual suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = apiClient.createSource(params, idempotencyKey)

    public actual suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = apiClient.retrieveSource(sourceId, clientSecret)


    public actual suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = apiClient.createPaymentMethod(params, idempotencyKey)

    public actual suspend fun retrievePaymentMethod(
        paymentMethodId: String
    ): StripeResult<PaymentMethod> = apiClient.retrievePaymentMethod(paymentMethodId)


    public actual suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent> = apiClient.retrievePaymentIntent(clientSecret)

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = apiClient.confirmPaymentIntent(params, idempotencyKey)

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
    public actual suspend fun handleNextActionForPayment(
        clientSecret: String
    ): StripeResult<PaymentIntent> = apiClient.handleNextActionForPayment(clientSecret)


    public actual suspend fun retrieveSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = apiClient.retrieveSetupIntent(clientSecret)

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = apiClient.confirmSetupIntent(params, idempotencyKey)

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
    public actual suspend fun handleNextActionForSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = apiClient.handleNextActionForSetupIntent(clientSecret)

    public actual companion object {
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
