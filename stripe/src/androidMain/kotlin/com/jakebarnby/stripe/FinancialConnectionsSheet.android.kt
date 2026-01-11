package com.jakebarnby.stripe

import androidx.activity.ComponentActivity
import com.jakebarnby.stripe.model.*
import com.stripe.android.financialconnections.FinancialConnectionsSheet as AndroidFinancialConnectionsSheet
import com.stripe.android.financialconnections.FinancialConnectionsSheetResult as AndroidFinancialConnectionsSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

// Use WeakReference to avoid memory leaks
private val currentFinancialConnectionsActivity = AtomicReference<WeakReference<ComponentActivity>?>(null)

/**
 * Set the current activity for presenting the Financial Connections Sheet.
 * This must be called from your Activity before using FinancialConnectionsSheet.
 *
 * IMPORTANT: This should be called in your Activity's onCreate() or onResume().
 * The activity reference is held weakly to prevent memory leaks.
 *
 * @param activity The current ComponentActivity
 */
public fun setFinancialConnectionsSheetActivity(activity: ComponentActivity) {
    currentFinancialConnectionsActivity.set(WeakReference(activity))
}

/**
 * Clear the current activity reference.
 * Call this in your Activity's onDestroy() to help with cleanup.
 */
public fun clearFinancialConnectionsSheetActivity() {
    currentFinancialConnectionsActivity.set(null)
}

/**
 * Android implementation of Financial Connections Sheet.
 *
 * THREAD SAFETY: All operations must be called from the main thread.
 * This class automatically ensures main thread execution using Dispatchers.Main.
 */
public actual class FinancialConnectionsSheet private constructor(
    private val configuration: FinancialConnectionsSheetConfiguration
) {
    public actual companion object {
        /**
         * Create a new Financial Connections Sheet instance.
         *
         * @param configuration The configuration for the sheet
         * @return A new FinancialConnectionsSheet instance
         */
        public actual fun create(
            configuration: FinancialConnectionsSheetConfiguration
        ): FinancialConnectionsSheet {
            return FinancialConnectionsSheet(configuration)
        }
    }

    private fun getActivity(): ComponentActivity {
        val activityRef = currentFinancialConnectionsActivity.get()
        val activity = activityRef?.get()
        return requireNotNull(activity) {
            "Activity not set or has been garbage collected. " +
            "Call setFinancialConnectionsSheetActivity() first."
        }
    }

    /**
     * Present the Financial Connections Sheet to the user.
     *
     * This method automatically ensures it runs on the main thread.
     * The continuation will be resumed when the user completes, cancels, or if an error occurs.
     *
     * @return The result of the Financial Connections flow
     */
    public actual suspend fun present(): FinancialConnectionsSheetResult {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    // Cleanup if coroutine is cancelled
                }

                val financialConnectionsSheet = AndroidFinancialConnectionsSheet.create(
                    activity = getActivity(),
                    callback = { result ->
                        val mappedResult = mapResult(result)
                        if (continuation.isActive) {
                            continuation.resume(mappedResult)
                        }
                    }
                )

                val androidConfig = AndroidFinancialConnectionsSheet.Configuration(
                    financialConnectionsSessionClientSecret = configuration.financialConnectionsSessionClientSecret,
                    publishableKey = configuration.publishableKey
                )

                financialConnectionsSheet.present(androidConfig)
            }
        }
    }

    /**
     * Present the Financial Connections Sheet and create a token.
     *
     * Note: The Android SDK doesn't directly support token creation.
     * Use the account ID from the session to create a token on your server.
     *
     * @return The result of the Financial Connections flow with token
     */
    public actual suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult {
        // Android SDK doesn't have a createForToken method
        // We present the sheet normally and inform the user to create token server-side
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    // Cleanup if coroutine is cancelled
                }

                val financialConnectionsSheet = AndroidFinancialConnectionsSheet.create(
                    activity = getActivity(),
                    callback = { result: AndroidFinancialConnectionsSheetResult ->
                        val mappedResult = mapTokenResult(result)
                        if (continuation.isActive) {
                            continuation.resume(mappedResult)
                        }
                    }
                )

                val androidConfig = AndroidFinancialConnectionsSheet.Configuration(
                    financialConnectionsSessionClientSecret = configuration.financialConnectionsSessionClientSecret,
                    publishableKey = configuration.publishableKey
                )

                financialConnectionsSheet.present(androidConfig)
            }
        }
    }

    private fun mapResult(
        result: AndroidFinancialConnectionsSheetResult
    ): FinancialConnectionsSheetResult {
        return when (result) {
            is AndroidFinancialConnectionsSheetResult.Completed -> {
                FinancialConnectionsSheetResult.Completed(
                    session = mapSession(result.financialConnectionsSession)
                )
            }
            is AndroidFinancialConnectionsSheetResult.Canceled -> {
                FinancialConnectionsSheetResult.Canceled
            }
            is AndroidFinancialConnectionsSheetResult.Failed -> {
                FinancialConnectionsSheetResult.Failed(
                    error = StripeException(
                        message = result.error.localizedMessage
                            ?: result.error.message
                            ?: "Financial Connections failed",
                        cause = result.error
                    )
                )
            }
        }
    }

    private fun mapTokenResult(
        result: AndroidFinancialConnectionsSheetResult
    ): FinancialConnectionsSheetForTokenResult {
        return when (result) {
            is AndroidFinancialConnectionsSheetResult.Completed -> {
                val session = result.financialConnectionsSession
                val accountsList = session.accounts.data
                val firstAccount = accountsList.firstOrNull()

                if (firstAccount == null) {
                    FinancialConnectionsSheetForTokenResult.Failed(
                        error = StripeException("No account was linked")
                    )
                } else {
                    // Note: Android SDK doesn't directly provide a token in the result.
                    // You would typically create a token on your server using the account ID.
                    // For now, we create a placeholder token structure.
                    FinancialConnectionsSheetForTokenResult.Failed(
                        error = StripeException(
                            "Token creation not directly supported on Android. " +
                            "Use the account ID '${firstAccount.id}' to create a token on your server."
                        )
                    )
                }
            }
            is AndroidFinancialConnectionsSheetResult.Canceled -> {
                FinancialConnectionsSheetForTokenResult.Canceled
            }
            is AndroidFinancialConnectionsSheetResult.Failed -> {
                FinancialConnectionsSheetForTokenResult.Failed(
                    error = StripeException(
                        message = result.error.localizedMessage
                            ?: result.error.message
                            ?: "Financial Connections failed",
                        cause = result.error
                    )
                )
            }
        }
    }

    private fun mapSession(
        androidSession: com.stripe.android.financialconnections.model.FinancialConnectionsSession
    ): FinancialConnectionsSession {
        return FinancialConnectionsSession(
            id = androidSession.id,
            clientSecret = androidSession.clientSecret,
            linkedAccounts = androidSession.accounts.data.map { mapLinkedAccount(it) },
            livemode = androidSession.livemode,
            returnUrl = null // Android SDK doesn't expose return URL
        )
    }

    private fun mapLinkedAccount(
        androidAccount: com.stripe.android.financialconnections.model.FinancialConnectionsAccount
    ): FinancialConnectionsLinkedAccount {
        return FinancialConnectionsLinkedAccount(
            id = androidAccount.id,
            institutionName = androidAccount.institutionName,
            displayName = androidAccount.displayName,
            last4 = androidAccount.last4,
            created = androidAccount.created.toLong(),
            balance = null, // Balance is not directly exposed in the current Android SDK version
            balanceRefresh = null, // BalanceRefresh is not directly exposed in the current Android SDK version
            category = mapAccountCategory(androidAccount.category),
            subcategory = androidAccount.subcategory?.let { mapAccountSubcategory(it) },
            supportedPaymentMethodTypes = androidAccount.supportedPaymentMethodTypes.map { it.value },
            status = mapLinkedAccountStatus(androidAccount.status),
            livemode = androidAccount.livemode
        )
    }

    private fun mapAccountCategory(
        category: com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category
    ): AccountCategory {
        return when (category) {
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category.CASH -> AccountCategory.CASH
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category.CREDIT -> AccountCategory.CREDIT
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category.INVESTMENT -> AccountCategory.INVESTMENT
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category.OTHER -> AccountCategory.OTHER
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Category.UNKNOWN -> AccountCategory.OTHER
        }
    }

    private fun mapAccountSubcategory(
        subcategory: com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory
    ): AccountSubcategory {
        return when (subcategory) {
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.CHECKING -> AccountSubcategory.CHECKING
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.SAVINGS -> AccountSubcategory.SAVINGS
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.CREDIT_CARD -> AccountSubcategory.CREDIT_CARD
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.LINE_OF_CREDIT -> AccountSubcategory.LINE_OF_CREDIT
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.MORTGAGE -> AccountSubcategory.MORTGAGE
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.OTHER -> AccountSubcategory.OTHER
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Subcategory.UNKNOWN -> AccountSubcategory.OTHER
        }
    }

    private fun mapLinkedAccountStatus(
        status: com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Status
    ): LinkedAccountStatus {
        return when (status) {
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Status.ACTIVE -> LinkedAccountStatus.ACTIVE
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Status.INACTIVE -> LinkedAccountStatus.INACTIVE
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Status.DISCONNECTED -> LinkedAccountStatus.DISCONNECTED
            com.stripe.android.financialconnections.model.FinancialConnectionsAccount.Status.UNKNOWN -> LinkedAccountStatus.DISCONNECTED
        }
    }
}

/**
 * Extension function to present Financial Connections Sheet directly from an Activity.
 */
public suspend fun ComponentActivity.presentFinancialConnectionsSheet(
    configuration: FinancialConnectionsSheetConfiguration
): FinancialConnectionsSheetResult {
    setFinancialConnectionsSheetActivity(this)
    return FinancialConnectionsSheet.create(configuration).present()
}
