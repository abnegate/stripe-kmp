package com.jakebarnby.stripe

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.jakebarnby.stripe.model.*
import com.stripe.android.PaymentIntentResult
import com.stripe.android.SetupIntentResult
import com.stripe.android.Stripe as AndroidStripe
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * Android implementation of PaymentAuthenticator using Stripe Android SDK's PaymentLauncher.
 *
 * This implementation uses:
 * - `PaymentLauncher` for handling 3DS authentication flows with Activity Result API
 * - Native 3DS2 SDK for 3DS2 challenges
 * - WeakReference for Activity to prevent memory leaks
 * - suspendCancellableCoroutine for async-to-suspend conversion
 *
 * IMPORTANT: The Android SDK requires Activity integration for authentication flows.
 * This class provides proper Activity Result API integration with lifecycle-safe callbacks.
 *
 * Note: Due to the Activity Result API requirement, authentication methods that require
 * an Activity context must be called from an Activity or properly managed lifecycle component.
 */
public actual class PaymentAuthenticator private constructor() {
    private val stripe: AndroidStripe by lazy {
        Stripe.getInstance().getAndroidStripe()
    }

    public actual companion object {
        @Volatile
        private var instance: PaymentAuthenticator? = null

        /**
         * Get the singleton instance of PaymentAuthenticator.
         */
        public actual fun getInstance(): PaymentAuthenticator {
            return instance ?: synchronized(this) {
                instance ?: PaymentAuthenticator().also { instance = it }
            }
        }
    }

    /**
     * Handle next action for a payment intent without activity context.
     *
     * On Android, most authentication flows require an Activity context for presenting
     * challenge screens or redirects. This method returns a failure indicating that
     * the activity-aware version should be used instead.
     */
    public actual suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult = withContext(Dispatchers.Main) {
        AuthenticationResult.Failed(
            error = StripeException(
                "Android authentication requires Activity context. " +
                "Use handleNextActionForPayment(activity, clientSecret) instead. " +
                "The activity parameter must be a ComponentActivity."
            )
        )
    }

    /**
     * Handle next action for a payment intent with Activity context.
     *
     * This method retrieves the PaymentIntent, checks if it requires action,
     * and presents the appropriate authentication flow (3DS, redirect, etc.) using PaymentLauncher.
     *
     * IMPLEMENTATION NOTE:
     * Due to Android's Activity Result API requirements, this implementation uses a coroutine-based
     * approach with PaymentLauncher. The launcher must be registered before the Activity is created,
     * so this method creates a temporary launcher for one-time use.
     *
     * For production applications with repeated authentication needs, consider:
     * 1. Registering PaymentLauncher in your Activity's onCreate
     * 2. Using PaymentSheet for a complete prebuilt UI
     * 3. Implementing custom Activity result handling
     */
    public actual suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = withContext(Dispatchers.IO) {
        try {
            require(activity is ComponentActivity) {
                "Activity must be a ComponentActivity for Android authentication. " +
                "ComponentActivity is required for the Activity Result API integration."
            }

            // First, retrieve the PaymentIntent to check its status
            val paymentIntentResult = stripe.retrievePaymentIntentSynchronous(clientSecret)
                ?: return@withContext AuthenticationResult.Failed(
                    error = StripeException("Failed to retrieve PaymentIntent")
                )

            // Check if action is required
            when (paymentIntentResult.status) {
                com.stripe.android.model.StripeIntent.Status.Succeeded -> {
                    // Already completed, return success
                    return@withContext AuthenticationResult.Completed(
                        paymentIntent = paymentIntentResult.toKmpPaymentIntent()
                    )
                }
                com.stripe.android.model.StripeIntent.Status.RequiresAction -> {
                    // Continue to authentication flow
                }
                com.stripe.android.model.StripeIntent.Status.Canceled -> {
                    return@withContext AuthenticationResult.Failed(
                        error = StripeException("PaymentIntent was canceled"),
                        paymentIntent = paymentIntentResult.toKmpPaymentIntent()
                    )
                }
                else -> {
                    // Return current state for other statuses
                    return@withContext AuthenticationResult.Completed(
                        paymentIntent = paymentIntentResult.toKmpPaymentIntent()
                    )
                }
            }

            // Handle the action using PaymentLauncher
            withContext(Dispatchers.Main) {
                handlePaymentAuthenticationFlow(activity, clientSecret)
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = when (e) {
                    is StripeException -> e
                    else -> StripeException(
                        message = e.message ?: "Authentication failed",
                        cause = e
                    )
                }
            )
        }
    }

    /**
     * Handle next action for a setup intent with Activity context.
     */
    public actual suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = withContext(Dispatchers.IO) {
        try {
            require(activity is ComponentActivity) {
                "Activity must be a ComponentActivity for Android authentication. " +
                "ComponentActivity is required for the Activity Result API integration."
            }

            // First, retrieve the SetupIntent to check its status
            val setupIntentResult = stripe.retrieveSetupIntentSynchronous(clientSecret)
                ?: return@withContext AuthenticationResult.Failed(
                    error = StripeException("Failed to retrieve SetupIntent")
                )

            // Check if action is required
            when (setupIntentResult.status) {
                com.stripe.android.model.StripeIntent.Status.Succeeded -> {
                    // Already completed, return success
                    return@withContext AuthenticationResult.Completed(
                        setupIntent = setupIntentResult.toKmpSetupIntent()
                    )
                }
                com.stripe.android.model.StripeIntent.Status.RequiresAction -> {
                    // Continue to authentication flow
                }
                com.stripe.android.model.StripeIntent.Status.Canceled -> {
                    return@withContext AuthenticationResult.Failed(
                        error = StripeException("SetupIntent was canceled"),
                        setupIntent = setupIntentResult.toKmpSetupIntent()
                    )
                }
                else -> {
                    // Return current state for other statuses
                    return@withContext AuthenticationResult.Completed(
                        setupIntent = setupIntentResult.toKmpSetupIntent()
                    )
                }
            }

            // Handle the action using PaymentLauncher
            withContext(Dispatchers.Main) {
                handleSetupAuthenticationFlow(activity, clientSecret)
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = when (e) {
                    is StripeException -> e
                    else -> StripeException(
                        message = e.message ?: "Authentication failed",
                        cause = e
                    )
                }
            )
        }
    }

    /**
     * Authenticate a payment intent end-to-end.
     *
     * This retrieves the PaymentIntent, and if it requires action, handles it.
     * This is a convenience wrapper around handleNextActionForPayment.
     */
    public actual suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return handleNextActionForPayment(activity, clientSecret)
    }

    /**
     * Authenticate a setup intent end-to-end.
     *
     * This retrieves the SetupIntent, and if it requires action, handles it.
     * This is a convenience wrapper around handleNextActionForSetupIntent.
     */
    public actual suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return handleNextActionForSetupIntent(activity, clientSecret)
    }

    /**
     * Handle 3DS2 challenge specifically.
     *
     * On Android, 3DS2 challenges are typically handled automatically by the SDK
     * when using handleNextAction. This method provides explicit challenge handling
     * for advanced use cases.
     */
    public actual suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse = withContext(Dispatchers.Main) {
        require(activity is ComponentActivity) {
            "Activity must be a ComponentActivity for Android 3DS2 challenge"
        }

        // Android SDK handles 3DS2 challenges through the standard nextAction flow
        // This method provides a response indicating the challenge should be handled
        // via the normal authentication flow
        Stripe3ds2AuthenticationResponse(
            id = challenge.threeDSecureServerTransactionId,
            ares = null,
            error = null,
            fallbackRedirectUrl = challenge.acsUrl,
            state = AuthenticationState.REDIRECT_REQUIRED
        )
    }

    // ============================================================================
    // Private helper methods
    // ============================================================================

    /**
     * Handle payment authentication flow with Activity integration using PaymentLauncher.
     *
     * IMPLEMENTATION NOTE:
     * The Android SDK's PaymentLauncher requires registration with the Activity Result API
     * before the Activity's onCreate completes. This creates a challenge for library code
     * that needs to launch authentication on-demand.
     *
     * This implementation uses a workaround approach:
     * 1. Creates a PaymentLauncher instance
     * 2. Uses suspendCancellableCoroutine to bridge async callbacks to coroutines
     * 3. Properly handles cancellation with invokeOnCancellation
     *
     * LIMITATION:
     * Due to Activity Result API constraints, this approach may not work in all scenarios.
     * For production applications, consider:
     * - Using PaymentSheet which handles all this complexity
     * - Registering PaymentLauncher in your Activity's onCreate and managing it yourself
     * - Creating a dedicated authentication Activity with proper lifecycle integration
     */
    private suspend fun handlePaymentAuthenticationFlow(
        activity: ComponentActivity,
        clientSecret: String
    ): AuthenticationResult = suspendCancellableCoroutine { continuation ->
        try {
            // Create WeakReference to avoid memory leaks
            val activityRef = WeakReference(activity)

            // Create PaymentLauncher with callback
            val paymentLauncher = PaymentLauncher.Companion.create(
                activity = activity,
                publishableKey = Stripe.getInstance().configuration.publishableKey,
                stripeAccountId = null,
                callback = object : PaymentLauncher.PaymentResultCallback {
                    override fun onPaymentResult(paymentResult: PaymentResult) {
                        when (paymentResult) {
                            is PaymentResult.Completed -> {
                                // Payment authentication completed successfully
                                // Retrieve the final PaymentIntent state
                                val act = activityRef.get()
                                if (act != null) {
                                    try {
                                        val paymentIntent = stripe.retrievePaymentIntentSynchronous(clientSecret)
                                        if (paymentIntent != null) {
                                            continuation.resume(
                                                AuthenticationResult.Completed(
                                                    paymentIntent = paymentIntent.toKmpPaymentIntent()
                                                )
                                            )
                                        } else {
                                            continuation.resume(
                                                AuthenticationResult.Failed(
                                                    error = StripeException("Failed to retrieve PaymentIntent after authentication")
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        continuation.resume(
                                            AuthenticationResult.Failed(
                                                error = StripeException(
                                                    message = "Error retrieving PaymentIntent: ${e.message}",
                                                    cause = e
                                                )
                                            )
                                        )
                                    }
                                } else {
                                    continuation.resume(
                                        AuthenticationResult.Failed(
                                            error = StripeException("Activity was destroyed during authentication")
                                        )
                                    )
                                }
                            }
                            is PaymentResult.Canceled -> {
                                continuation.resume(AuthenticationResult.Canceled)
                            }
                            is PaymentResult.Failed -> {
                                continuation.resume(
                                    AuthenticationResult.Failed(
                                        error = StripeException(
                                            message = paymentResult.throwable.message ?: "Authentication failed",
                                            cause = paymentResult.throwable
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Set up cancellation handler
            continuation.invokeOnCancellation {
                // Clean up resources if coroutine is cancelled
                activityRef.clear()
            }

            // Launch the authentication flow
            paymentLauncher.handleNextActionForPaymentIntent(clientSecret)
        } catch (e: Exception) {
            continuation.resume(
                AuthenticationResult.Failed(
                    error = StripeException(
                        message = e.message ?: "Failed to start authentication flow",
                        cause = e
                    )
                )
            )
        }
    }

    /**
     * Handle setup authentication flow with Activity integration using PaymentLauncher.
     */
    private suspend fun handleSetupAuthenticationFlow(
        activity: ComponentActivity,
        clientSecret: String
    ): AuthenticationResult = suspendCancellableCoroutine { continuation ->
        try {
            // Create WeakReference to avoid memory leaks
            val activityRef = WeakReference(activity)

            // Create PaymentLauncher with callback
            val paymentLauncher = PaymentLauncher.Companion.create(
                activity = activity,
                publishableKey = Stripe.getInstance().configuration.publishableKey,
                stripeAccountId = null,
                callback = object : PaymentLauncher.PaymentResultCallback {
                    override fun onPaymentResult(paymentResult: PaymentResult) {
                        when (paymentResult) {
                            is PaymentResult.Completed -> {
                                // Setup authentication completed successfully
                                // Retrieve the final SetupIntent state
                                val act = activityRef.get()
                                if (act != null) {
                                    try {
                                        val setupIntent = stripe.retrieveSetupIntentSynchronous(clientSecret)
                                        if (setupIntent != null) {
                                            continuation.resume(
                                                AuthenticationResult.Completed(
                                                    setupIntent = setupIntent.toKmpSetupIntent()
                                                )
                                            )
                                        } else {
                                            continuation.resume(
                                                AuthenticationResult.Failed(
                                                    error = StripeException("Failed to retrieve SetupIntent after authentication")
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        continuation.resume(
                                            AuthenticationResult.Failed(
                                                error = StripeException(
                                                    message = "Error retrieving SetupIntent: ${e.message}",
                                                    cause = e
                                                )
                                            )
                                        )
                                    }
                                } else {
                                    continuation.resume(
                                        AuthenticationResult.Failed(
                                            error = StripeException("Activity was destroyed during authentication")
                                        )
                                    )
                                }
                            }
                            is PaymentResult.Canceled -> {
                                continuation.resume(AuthenticationResult.Canceled)
                            }
                            is PaymentResult.Failed -> {
                                continuation.resume(
                                    AuthenticationResult.Failed(
                                        error = StripeException(
                                            message = paymentResult.throwable.message ?: "Authentication failed",
                                            cause = paymentResult.throwable
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            )

            // Set up cancellation handler
            continuation.invokeOnCancellation {
                // Clean up resources if coroutine is cancelled
                activityRef.clear()
            }

            // Launch the authentication flow
            paymentLauncher.handleNextActionForSetupIntent(clientSecret)
        } catch (e: Exception) {
            continuation.resume(
                AuthenticationResult.Failed(
                    error = StripeException(
                        message = e.message ?: "Failed to start authentication flow",
                        cause = e
                    )
                )
            )
        }
    }
}
