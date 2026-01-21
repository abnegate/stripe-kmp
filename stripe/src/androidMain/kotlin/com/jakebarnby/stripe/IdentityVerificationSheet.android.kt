package com.jakebarnby.stripe

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.jakebarnby.stripe.model.*
import com.stripe.android.identity.IdentityVerificationSheet as AndroidIdentityVerificationSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

private val currentIdentityActivity = AtomicReference<WeakReference<ComponentActivity>?>(null)

/**
 * Set the current activity for presenting the Identity Verification Sheet.
 * This must be called from your Activity before using IdentityVerificationSheet.
 *
 * IMPORTANT: This should be called in your Activity's onCreate() or onResume().
 * The activity reference is held weakly to prevent memory leaks.
 *
 * @param activity The current ComponentActivity
 */
public fun setIdentityVerificationSheetActivity(activity: ComponentActivity) {
    currentIdentityActivity.set(WeakReference(activity))
}

/**
 * Clear the current identity activity reference.
 * Call this in your Activity's onDestroy() to help with cleanup.
 */
public fun clearIdentityVerificationSheetActivity() {
    currentIdentityActivity.set(null)
}

/**
 * Android implementation of IdentityVerificationSheet.
 *
 * This implementation uses the Stripe Identity Android SDK to present
 * a native verification flow for identity document and selfie verification.
 *
 * THREAD SAFETY: All operations must be called from the main thread.
 * This class automatically ensures main thread execution using Dispatchers.Main.
 */
public actual class IdentityVerificationSheet {
    private fun getActivity(): ComponentActivity {
        val activityRef = currentIdentityActivity.get()
        val activity = activityRef?.get()
        return requireNotNull(activity) {
            "Activity not set or has been garbage collected. Call setIdentityVerificationSheetActivity() first."
        }
    }

    /**
     * Present the Identity Verification Sheet with the given configuration.
     *
     * This method automatically ensures it runs on the main thread.
     * The coroutine will suspend until the user completes, cancels, or if an error occurs.
     *
     * @param configuration The verification session configuration
     * @return The result of the verification flow
     */
    public actual suspend fun present(
        configuration: IdentityVerificationSheetConfiguration
    ): IdentityVerificationSheetResult {
        // Ensure main thread execution
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                // Add cancellation handler
                continuation.invokeOnCancellation {
                    // Return canceled result if coroutine is cancelled
                    if (continuation.isActive) {
                        continuation.resume(IdentityVerificationSheetResult.Canceled)
                    }
                }

                // Create fresh instance each time - don't reuse
                val activity = getActivity()
                val identityVerificationSheet = AndroidIdentityVerificationSheet.create(
                    from = activity,
                    configuration = buildAndroidConfiguration(configuration),
                    identityVerificationCallback = { result ->
                        val mappedResult = when (result) {
                            is AndroidIdentityVerificationSheet.VerificationFlowResult.Completed -> {
                                // Verification flow completed - create a session object
                                // Note: The Android SDK doesn't return full session details,
                                // so we create a minimal session object. The app should fetch
                                // full details from the server.
                                // We construct a placeholder client secret that matches the required format
                                val placeholderSecret = "${configuration.verificationSessionId}_secret_android"
                                val session = IdentityVerificationSession(
                                    id = configuration.verificationSessionId,
                                    clientSecret = placeholderSecret,
                                    status = VerificationSessionStatus.PROCESSING,
                                    type = VerificationType.DOCUMENT,
                                    livemode = false,
                                    created = System.currentTimeMillis() / 1000,
                                    lastVerificationReport = null,
                                    metadata = emptyMap()
                                )
                                IdentityVerificationSheetResult.Completed(session)
                            }
                            is AndroidIdentityVerificationSheet.VerificationFlowResult.Canceled -> {
                                IdentityVerificationSheetResult.Canceled
                            }
                            is AndroidIdentityVerificationSheet.VerificationFlowResult.Failed -> {
                                val error = result.throwable
                                IdentityVerificationSheetResult.Failed(
                                    StripeException(
                                        message = error.localizedMessage ?: error.message ?: "Identity verification failed",
                                        cause = error
                                    )
                                )
                            }
                        }

                        // Resume coroutine with result
                        if (continuation.isActive) {
                            continuation.resume(mappedResult)
                        }
                    }
                )

                // Present the verification sheet
                identityVerificationSheet.present(
                    verificationSessionId = configuration.verificationSessionId,
                    ephemeralKeySecret = configuration.ephemeralKeySecret
                )
            }
        }
    }

    private fun buildAndroidConfiguration(
        config: IdentityVerificationSheetConfiguration
    ): AndroidIdentityVerificationSheet.Configuration {
        // Build configuration with brand logo if provided
        // Note: The Android SDK expects a Uri for brandLogo
        // Since we're passing a string in the KMP interface, we convert it to Uri if available
        // Otherwise, use an empty Uri as placeholder
        val brandLogoUri = config.brandLogo?.toUri() ?: Uri.EMPTY
        return AndroidIdentityVerificationSheet.Configuration(
            brandLogo = brandLogoUri
        )
    }
}

/**
 * Extension function to present Identity Verification Sheet directly from an Activity.
 *
 * This is a convenience method that sets the activity and presents the sheet.
 *
 * @param configuration The verification session configuration
 * @return The result of the verification flow
 */
public suspend fun ComponentActivity.presentIdentityVerificationSheet(
    configuration: IdentityVerificationSheetConfiguration
): IdentityVerificationSheetResult {
    setIdentityVerificationSheetActivity(this)
    return IdentityVerificationSheet().present(configuration)
}
