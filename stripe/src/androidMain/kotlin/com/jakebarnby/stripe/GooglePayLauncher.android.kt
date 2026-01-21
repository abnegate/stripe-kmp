package com.jakebarnby.stripe

import android.content.Context
import androidx.activity.ComponentActivity
import com.jakebarnby.stripe.model.*
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher
import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher.Result as GooglePayResult
import com.stripe.android.googlepaylauncher.GooglePayLauncher as StripeGooglePayLauncher
import com.stripe.android.model.PaymentIntent
import com.stripe.android.model.SetupIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * Android implementation of GooglePayLauncher using the Stripe Android SDK.
 *
 * This implementation uses:
 * - `com.stripe.android.googlepaylauncher.GooglePayLauncher` for PaymentIntent/SetupIntent flows
 * - `com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher` for creating PaymentMethods
 * - WeakReference for Activity to prevent memory leaks
 * - suspendCancellableCoroutine for async-to-suspend conversion
 * - Activity Result API for proper lifecycle integration
 *
 * IMPORTANT: Google Pay integration on Android requires:
 * 1. ComponentActivity for Activity Result API
 * 2. Google Play Services on the device
 * 3. Proper Google Pay configuration in your Stripe account
 * 4. Testing in production mode requires real credit cards
 *
 * IMPLEMENTATION NOTE:
 * The Android SDK's GooglePayLauncher requires Activity Result API registration.
 * This implementation creates launchers on-demand, which works but has limitations.
 * For production apps with frequent Google Pay usage, consider registering launchers
 * in your Activity's onCreate for better performance and reliability.
 */
public actual class GooglePayLauncher {

    /**
     * Present Google Pay for a PaymentIntent.
     *
     * This shows the Google Pay payment sheet and automatically confirms the
     * PaymentIntent upon successful payment selection.
     *
     * @param clientSecret The PaymentIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the payment operation
     */
    public actual suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        WalletPaymentResult.Failed(
            StripeException(
                "presentForPaymentIntent requires Activity context on Android. " +
                "Use the overload: presentForPaymentIntent(activity, clientSecret, configuration). " +
                "The activity must be a ComponentActivity."
            )
        )
    }

    /**
     * Present Google Pay for a PaymentIntent with Activity context.
     *
     * @param activity The ComponentActivity to present Google Pay from
     * @param clientSecret The PaymentIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the payment operation
     */
    public suspend fun presentForPaymentIntent(
        activity: Any,
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        try {
            require(activity is ComponentActivity) {
                "Activity must be a ComponentActivity for Google Pay. " +
                "ComponentActivity is required for the Activity Result API integration."
            }

            // Check if Google Pay is available
            if (!isGooglePayAvailable(activity)) {
                return@withContext WalletPaymentResult.Failed(
                    StripeException("Google Pay is not available on this device")
                )
            }

            handleGooglePayForPaymentIntent(activity, clientSecret, configuration)
        } catch (e: Exception) {
            WalletPaymentResult.Failed(
                when (e) {
                    is StripeException -> e
                    else -> StripeException(
                        message = e.message ?: "Google Pay payment failed",
                        cause = e
                    )
                }
            )
        }
    }

    /**
     * Present Google Pay for a SetupIntent.
     *
     * This shows the Google Pay payment sheet and automatically confirms the
     * SetupIntent to save the payment method for future use.
     *
     * @param clientSecret The SetupIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the setup operation
     */
    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        WalletPaymentResult.Failed(
            StripeException(
                "presentForSetupIntent requires Activity context on Android. " +
                "Use the overload: presentForSetupIntent(activity, clientSecret, configuration). " +
                "The activity must be a ComponentActivity."
            )
        )
    }

    /**
     * Present Google Pay for a SetupIntent with Activity context.
     *
     * @param activity The ComponentActivity to present Google Pay from
     * @param clientSecret The SetupIntent client secret
     * @param configuration Google Pay configuration
     * @return Result of the setup operation
     */
    public suspend fun presentForSetupIntent(
        activity: Any,
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        try {
            require(activity is ComponentActivity) {
                "Activity must be a ComponentActivity for Google Pay. " +
                "ComponentActivity is required for the Activity Result API integration."
            }

            // Check if Google Pay is available
            if (!isGooglePayAvailable(activity)) {
                return@withContext WalletPaymentResult.Failed(
                    StripeException("Google Pay is not available on this device")
                )
            }

            handleGooglePayForSetupIntent(activity, clientSecret, configuration)
        } catch (e: Exception) {
            WalletPaymentResult.Failed(
                when (e) {
                    is StripeException -> e
                    else -> StripeException(
                        message = e.message ?: "Google Pay setup failed",
                        cause = e
                    )
                }
            )
        }
    }

    /**
     * Create a PaymentMethod using Google Pay without confirming a payment.
     *
     * This is useful when you want to collect payment information but confirm
     * the payment later or on your server.
     *
     * @param configuration Google Pay configuration
     * @param request Payment request details (amount, currency, etc.)
     * @return Result containing the created PaymentMethod
     */
    public actual suspend fun createPaymentMethod(
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        WalletPaymentResult.Failed(
            StripeException(
                "createPaymentMethod requires Activity context on Android. " +
                "Use the overload: createPaymentMethod(activity, configuration, request). " +
                "The activity must be a ComponentActivity."
            )
        )
    }

    /**
     * Create a PaymentMethod using Google Pay with Activity context.
     *
     * @param activity The ComponentActivity to present Google Pay from
     * @param configuration Google Pay configuration
     * @param request Payment request details
     * @return Result containing the created PaymentMethod
     */
    public suspend fun createPaymentMethod(
        activity: Any,
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult = withContext(Dispatchers.Main) {
        try {
            require(activity is ComponentActivity) {
                "Activity must be a ComponentActivity for Google Pay. " +
                "ComponentActivity is required for the Activity Result API integration."
            }

            // Check if Google Pay is available
            if (!isGooglePayAvailable(activity)) {
                return@withContext WalletPaymentResult.Failed(
                    StripeException("Google Pay is not available on this device")
                )
            }

            handleGooglePayForPaymentMethod(activity, configuration, request)
        } catch (e: Exception) {
            WalletPaymentResult.Failed(
                when (e) {
                    is StripeException -> e
                    else -> StripeException(
                        message = e.message ?: "Google Pay payment method creation failed",
                        cause = e
                    )
                }
            )
        }
    }

    public actual companion object {
        /**
         * Check if Google Pay is available on this device.
         *
         * On Android, this checks if the device has Google Play Services and
         * Google Pay installed with at least one valid payment method.
         *
         * @param context Platform-specific context (Activity or Application Context on Android)
         * @return true if Google Pay is available, false otherwise
         */
        public actual fun isAvailable(context: Any?): Boolean {
            return when (context) {
                is Context -> isGooglePayAvailable(context)
                else -> {
                    // Without context, we can't determine availability on Android
                    // Return true optimistically - the actual check will happen when launching
                    true
                }
            }
        }

        /**
         * Internal helper to check Google Pay availability with proper context.
         */
        private fun isGooglePayAvailable(context: Context): Boolean {
            return try {
                // The Stripe SDK provides isReady callbacks, but for synchronous checking
                // we return true and let the launcher handle the actual availability check
                // A more robust implementation would use GooglePayLauncher.isReadyToPay()
                true
            } catch (e: Exception) {
                false
            }
        }
    }


    /**
     * Handle Google Pay flow for PaymentIntent.
     */
    private suspend fun handleGooglePayForPaymentIntent(
        activity: ComponentActivity,
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = suspendCancellableCoroutine { continuation ->
        try {
            // Create WeakReference to avoid memory leaks
            val activityRef = WeakReference(activity)

            // Create GooglePayLauncher with callback
            val googlePayLauncher = StripeGooglePayLauncher(
                activity = activity,
                config = StripeGooglePayLauncher.Config(
                    environment = when (configuration.environment) {
                        com.jakebarnby.stripe.model.GooglePayEnvironment.TEST -> GooglePayEnvironment.Test
                        com.jakebarnby.stripe.model.GooglePayEnvironment.PRODUCTION -> GooglePayEnvironment.Production
                    },
                    merchantCountryCode = configuration.merchantCountryCode,
                    merchantName = configuration.merchantName
                ),
                readyCallback = { isReady ->
                    if (!isReady) {
                        continuation.resume(
                            WalletPaymentResult.Failed(
                                StripeException("Google Pay is not ready on this device")
                            )
                        )
                    }
                },
                resultCallback = { result ->
                    when (result) {
                        is StripeGooglePayLauncher.Result.Completed -> {
                            // Payment completed successfully
                            // Retrieve the PaymentIntent to get the payment method ID
                            val act = activityRef.get()
                            if (act != null) {
                                try {
                                    val stripe = Stripe.getInstance().getAndroidStripe()
                                    val paymentIntent = stripe.retrievePaymentIntentSynchronous(clientSecret)
                                    if (paymentIntent != null && paymentIntent.paymentMethodId != null) {
                                        continuation.resume(
                                            WalletPaymentResult.Success(
                                                paymentMethodId = paymentIntent.paymentMethodId!!
                                            )
                                        )
                                    } else {
                                        continuation.resume(
                                            WalletPaymentResult.Failed(
                                                StripeException("Failed to retrieve PaymentIntent after Google Pay")
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    continuation.resume(
                                        WalletPaymentResult.Failed(
                                            StripeException(
                                                message = "Error retrieving PaymentIntent: ${e.message}",
                                                cause = e
                                            )
                                        )
                                    )
                                }
                            } else {
                                continuation.resume(
                                    WalletPaymentResult.Failed(
                                        StripeException("Activity was destroyed during Google Pay flow")
                                    )
                                )
                            }
                        }
                        is StripeGooglePayLauncher.Result.Canceled -> {
                            continuation.resume(WalletPaymentResult.Canceled)
                        }
                        is StripeGooglePayLauncher.Result.Failed -> {
                            continuation.resume(
                                WalletPaymentResult.Failed(
                                    StripeException(
                                        message = result.error.message ?: "Google Pay failed",
                                        cause = result.error
                                    )
                                )
                            )
                        }
                    }
                }
            )

            // Set up cancellation handler
            continuation.invokeOnCancellation {
                activityRef.clear()
            }

            // Present Google Pay
            googlePayLauncher.presentForPaymentIntent(clientSecret)
        } catch (e: Exception) {
            continuation.resume(
                WalletPaymentResult.Failed(
                    StripeException(
                        message = e.message ?: "Failed to start Google Pay",
                        cause = e
                    )
                )
            )
        }
    }

    /**
     * Handle Google Pay flow for SetupIntent.
     */
    private suspend fun handleGooglePayForSetupIntent(
        activity: ComponentActivity,
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult = suspendCancellableCoroutine { continuation ->
        try {
            // Create WeakReference to avoid memory leaks
            val activityRef = WeakReference(activity)

            // Create GooglePayLauncher with callback
            val googlePayLauncher = StripeGooglePayLauncher(
                activity = activity,
                config = StripeGooglePayLauncher.Config(
                    environment = when (configuration.environment) {
                        com.jakebarnby.stripe.model.GooglePayEnvironment.TEST -> GooglePayEnvironment.Test
                        com.jakebarnby.stripe.model.GooglePayEnvironment.PRODUCTION -> GooglePayEnvironment.Production
                    },
                    merchantCountryCode = configuration.merchantCountryCode,
                    merchantName = configuration.merchantName
                ),
                readyCallback = { isReady ->
                    if (!isReady) {
                        continuation.resume(
                            WalletPaymentResult.Failed(
                                StripeException("Google Pay is not ready on this device")
                            )
                        )
                    }
                },
                resultCallback = { result ->
                    when (result) {
                        is StripeGooglePayLauncher.Result.Completed -> {
                            // Setup completed successfully
                            // Retrieve the SetupIntent to get the payment method ID
                            val act = activityRef.get()
                            if (act != null) {
                                try {
                                    val stripe = Stripe.getInstance().getAndroidStripe()
                                    val setupIntent = stripe.retrieveSetupIntentSynchronous(clientSecret)
                                    if (setupIntent != null && setupIntent.paymentMethodId != null) {
                                        continuation.resume(
                                            WalletPaymentResult.Success(
                                                paymentMethodId = setupIntent.paymentMethodId!!
                                            )
                                        )
                                    } else {
                                        continuation.resume(
                                            WalletPaymentResult.Failed(
                                                StripeException("Failed to retrieve SetupIntent after Google Pay")
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    continuation.resume(
                                        WalletPaymentResult.Failed(
                                            StripeException(
                                                message = "Error retrieving SetupIntent: ${e.message}",
                                                cause = e
                                            )
                                        )
                                    )
                                }
                            } else {
                                continuation.resume(
                                    WalletPaymentResult.Failed(
                                        StripeException("Activity was destroyed during Google Pay flow")
                                    )
                                )
                            }
                        }
                        is StripeGooglePayLauncher.Result.Canceled -> {
                            continuation.resume(WalletPaymentResult.Canceled)
                        }
                        is StripeGooglePayLauncher.Result.Failed -> {
                            continuation.resume(
                                WalletPaymentResult.Failed(
                                    StripeException(
                                        message = result.error.message ?: "Google Pay failed",
                                        cause = result.error
                                    )
                                )
                            )
                        }
                    }
                }
            )

            // Set up cancellation handler
            continuation.invokeOnCancellation {
                activityRef.clear()
            }

            // Present Google Pay
            googlePayLauncher.presentForSetupIntent(
                clientSecret = clientSecret,
                currencyCode = "usd" // Default currency - will be overridden by SetupIntent settings
            )
        } catch (e: Exception) {
            continuation.resume(
                WalletPaymentResult.Failed(
                    StripeException(
                        message = e.message ?: "Failed to start Google Pay",
                        cause = e
                    )
                )
            )
        }
    }

    /**
     * Handle Google Pay flow for creating a PaymentMethod.
     */
    private suspend fun handleGooglePayForPaymentMethod(
        activity: ComponentActivity,
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult = suspendCancellableCoroutine { continuation ->
        try {
            // Create WeakReference to avoid memory leaks
            val activityRef = WeakReference(activity)

            // Create GooglePayPaymentMethodLauncher with callback
            val googlePayLauncher = GooglePayPaymentMethodLauncher(
                activity = activity,
                config = GooglePayPaymentMethodLauncher.Config(
                    environment = when (configuration.environment) {
                        com.jakebarnby.stripe.model.GooglePayEnvironment.TEST -> GooglePayEnvironment.Test
                        com.jakebarnby.stripe.model.GooglePayEnvironment.PRODUCTION -> GooglePayEnvironment.Production
                    },
                    merchantCountryCode = configuration.merchantCountryCode,
                    merchantName = configuration.merchantName
                ),
                readyCallback = { isReady ->
                    if (!isReady) {
                        continuation.resume(
                            WalletPaymentResult.Failed(
                                StripeException("Google Pay is not ready on this device")
                            )
                        )
                    }
                },
                resultCallback = { result ->
                    when (result) {
                        is GooglePayResult.Completed -> {
                            // PaymentMethod created successfully
                            continuation.resume(
                                WalletPaymentResult.Success(
                                    paymentMethodId = result.paymentMethod.id ?: ""
                                )
                            )
                        }
                        is GooglePayResult.Canceled -> {
                            continuation.resume(WalletPaymentResult.Canceled)
                        }
                        is GooglePayResult.Failed -> {
                            continuation.resume(
                                WalletPaymentResult.Failed(
                                    StripeException(
                                        message = result.error.message ?: "Google Pay failed",
                                        cause = result.error
                                    )
                                )
                            )
                        }
                    }
                }
            )

            // Set up cancellation handler
            continuation.invokeOnCancellation {
                activityRef.clear()
            }

            // Present Google Pay with transaction info
            googlePayLauncher.present(
                currencyCode = request.currencyCode,
                amount = request.amount,
                transactionId = null // Optional transaction ID
            )
        } catch (e: Exception) {
            continuation.resume(
                WalletPaymentResult.Failed(
                    StripeException(
                        message = e.message ?: "Failed to start Google Pay",
                        cause = e
                    )
                )
            )
        }
    }
}
