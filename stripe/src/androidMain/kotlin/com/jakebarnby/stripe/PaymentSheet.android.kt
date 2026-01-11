package com.jakebarnby.stripe

import androidx.activity.ComponentActivity
import com.stripe.android.paymentsheet.PaymentSheet as AndroidPaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult as AndroidPaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

// CRITICAL-07: Use WeakReference to avoid memory leaks
private val currentActivity = AtomicReference<WeakReference<ComponentActivity>?>(null)

/**
 * Set the current activity for presenting the Payment Sheet.
 * This must be called from your Activity before using PaymentSheet.
 *
 * IMPORTANT: This should be called in your Activity's onCreate() or onResume().
 * The activity reference is held weakly to prevent memory leaks.
 *
 * @param activity The current ComponentActivity
 */
public fun setPaymentSheetActivity(activity: ComponentActivity) {
    // CRITICAL-07: Store activity in WeakReference to prevent memory leak
    currentActivity.set(WeakReference(activity))
}

/**
 * Clear the current activity reference.
 * Call this in your Activity's onDestroy() to help with cleanup.
 */
public fun clearPaymentSheetActivity() {
    currentActivity.set(null)
}

/**
 * Android implementation of PaymentSheet.
 *
 * THREAD SAFETY: All PaymentSheet operations must be called from the main thread.
 * This class automatically ensures main thread execution using Dispatchers.Main.
 */
public actual class PaymentSheet {
    private fun getActivity(): ComponentActivity {
        val activityRef = currentActivity.get()
        val activity = activityRef?.get()
        requireNotNull(activity) {
            "Activity not set or has been garbage collected. Call setPaymentSheetActivity() first."
        }

        // Check if activity is in a valid state for presenting UI
        if (activity.isFinishing) {
            throw IllegalStateException("Activity is finishing - cannot present PaymentSheet")
        }
        if (activity.isDestroyed) {
            throw IllegalStateException("Activity is destroyed - cannot present PaymentSheet")
        }

        return activity
    }

    // HIGH-08: Create fresh PaymentSheet instance each time, don't reuse
    private fun createPaymentSheet(callback: (PaymentSheetResult) -> Unit): AndroidPaymentSheet {
        return AndroidPaymentSheet(getActivity()) { result ->
            // LOW-01: Map to proper Stripe error codes
            val mappedResult = when (result) {
                is AndroidPaymentSheetResult.Completed -> PaymentSheetResult.Completed
                is AndroidPaymentSheetResult.Canceled -> PaymentSheetResult.Canceled
                is AndroidPaymentSheetResult.Failed -> {
                    val error = result.error
                    PaymentSheetResult.Failed(
                        StripeError(
                            message = error.localizedMessage ?: error.message ?: "Payment failed",
                            code = when {
                                error is com.stripe.android.core.exception.InvalidRequestException -> "invalid_request"
                                error is com.stripe.android.core.exception.AuthenticationException -> "authentication_error"
                                error is com.stripe.android.core.exception.APIConnectionException -> "api_connection_error"
                                error is com.stripe.android.core.exception.APIException -> "api_error"
                                else -> "unknown_error"
                            }
                        )
                    )
                }
            }
            callback(mappedResult)
        }
    }

    /**
     * Present the Payment Sheet with a PaymentIntent.
     *
     * This method automatically ensures it runs on the main thread.
     * The continuation will be resumed when the user completes, cancels, or if an error occurs.
     *
     * @param configuration The PaymentIntent configuration
     * @param onResult Callback invoked with the payment result
     */
    public actual suspend fun presentWithPaymentIntent(
        configuration: PaymentIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        // MEDIUM-02: Ensure main thread execution
        withContext(Dispatchers.Main) {
            // MEDIUM-06: Use suspendCancellableCoroutine for proper cancellation support
            suspendCancellableCoroutine { continuation ->
                // HIGH-02: Add cancellation handler
                continuation.invokeOnCancellation {
                    // Cleanup if coroutine is cancelled
                    onResult(PaymentSheetResult.Canceled)
                }

                // HIGH-08: Create fresh instance each time
                val paymentSheet = createPaymentSheet { result ->
                    onResult(result)
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                val androidConfig = buildAndroidConfiguration(configuration.paymentSheetConfiguration)
                paymentSheet.presentWithPaymentIntent(
                    paymentIntentClientSecret = configuration.clientSecret,
                    configuration = androidConfig
                )
            }
        }
    }

    /**
     * Present the Payment Sheet with a SetupIntent.
     *
     * This method automatically ensures it runs on the main thread.
     * The continuation will be resumed when the user completes, cancels, or if an error occurs.
     *
     * @param configuration The SetupIntent configuration
     * @param onResult Callback invoked with the result
     */
    public actual suspend fun presentWithSetupIntent(
        configuration: SetupIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        // MEDIUM-02: Ensure main thread execution
        withContext(Dispatchers.Main) {
            // MEDIUM-06: Use suspendCancellableCoroutine for proper cancellation support
            suspendCancellableCoroutine { continuation ->
                // HIGH-02: Add cancellation handler
                continuation.invokeOnCancellation {
                    // Cleanup if coroutine is cancelled
                    onResult(PaymentSheetResult.Canceled)
                }

                // HIGH-08: Create fresh instance each time
                val paymentSheet = createPaymentSheet { result ->
                    onResult(result)
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                val androidConfig = buildAndroidConfiguration(configuration.paymentSheetConfiguration)
                paymentSheet.presentWithSetupIntent(
                    setupIntentClientSecret = configuration.clientSecret,
                    configuration = androidConfig
                )
            }
        }
    }

    private fun buildAndroidConfiguration(
        config: PaymentSheetConfiguration
    ): AndroidPaymentSheet.Configuration {
        return AndroidPaymentSheet.Configuration(
            merchantDisplayName = config.merchantDisplayName,
            customer = config.customerId?.let { customerId ->
                config.customerEphemeralKeySecret?.let { ephemeralKey ->
                    AndroidPaymentSheet.CustomerConfiguration(
                        id = customerId,
                        ephemeralKeySecret = ephemeralKey
                    )
                }
            },
            allowsDelayedPaymentMethods = config.allowsDelayedPaymentMethods
        )
    }
}

/**
 * Extension function to present PaymentSheet with PaymentIntent directly from an Activity.
 */
public suspend fun ComponentActivity.presentPaymentSheet(
    configuration: PaymentIntentConfiguration,
    onResult: (PaymentSheetResult) -> Unit
) {
    setPaymentSheetActivity(this)
    PaymentSheet().presentWithPaymentIntent(configuration, onResult)
}

/**
 * Extension function to present PaymentSheet with SetupIntent directly from an Activity.
 */
public suspend fun ComponentActivity.presentSetupSheet(
    configuration: SetupIntentConfiguration,
    onResult: (PaymentSheetResult) -> Unit
) {
    setPaymentSheetActivity(this)
    PaymentSheet().presentWithSetupIntent(configuration, onResult)
}
