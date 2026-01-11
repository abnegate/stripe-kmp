package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventListener
import kotlin.coroutines.resume
import kotlin.js.Promise

/**
 * External declarations for Stripe.js Financial Connections API
 */
public external interface StripeCollectBankAccountResult {
    public val error: StripeJsError?
    public val financialConnectionsSession: dynamic
}

public external interface StripeCollectBankAccountOptions {
    public var clientSecret: String
    public var params: dynamic
}

public external interface StripeInstanceFinancialConnections : StripeInstance {
    public fun collectBankAccountToken(options: StripeCollectBankAccountOptions): Promise<StripeCollectBankAccountResult>
}

/**
 * JS Financial Connections Sheet implementation using Stripe.js.
 *
 * Financial Connections in JavaScript is integrated through Stripe.js collectBankAccountToken API.
 * It can also be used automatically via the Payment Element when selecting US Bank Account.
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

    /**
     * Present the Financial Connections Sheet to the user.
     *
     * This uses Stripe.js collectBankAccountToken to present the Financial Connections flow.
     *
     * @return The result of the Financial Connections flow
     */
    public actual suspend fun present(): FinancialConnectionsSheetResult {
        return suspendCancellableCoroutine { continuation ->
            val stripe = Stripe.getInstance()

            val stripeJs = stripe.stripeInstance?.unsafeCast<StripeInstanceFinancialConnections>()
            if (stripeJs == null) {
                val result = FinancialConnectionsSheetResult.Failed(
                    error = StripeException(
                        message = "Stripe.js not loaded. Call Stripe.initialize() and wait for load to complete.",
                        stripeError = null,
                        statusCode = null,
                        requestId = null,
                        cause = null
                    )
                )
                if (continuation.isActive) {
                    continuation.resume(result)
                }
                return@suspendCancellableCoroutine
            }

            // Collect bank account using Financial Connections
            val options = object : StripeCollectBankAccountOptions {
                override var clientSecret = configuration.financialConnectionsSessionClientSecret
                override var params = object {
                    val payment_method_type = "us_bank_account"
                }
            }

            stripeJs.collectBankAccountToken(options).then { result ->
                val mappedResult = if (result.error != null) {
                    FinancialConnectionsSheetResult.Failed(
                        error = StripeException(
                            message = result.error!!.message,
                            stripeError = StripeError(
                                type = "api_error",
                                code = result.error!!.code,
                                message = result.error!!.message
                            ),
                            statusCode = null,
                            requestId = null,
                            cause = null
                        )
                    )
                } else if (result.financialConnectionsSession != null) {
                    FinancialConnectionsSheetResult.Completed(
                        session = mapSession(result.financialConnectionsSession)
                    )
                } else {
                    FinancialConnectionsSheetResult.Failed(
                        error = StripeException(
                            message = "Unknown error occurred",
                            stripeError = null,
                            statusCode = null,
                            requestId = null,
                            cause = null
                        )
                    )
                }

                if (continuation.isActive) {
                    continuation.resume(mappedResult)
                }
            }.catch { error ->
                val result = FinancialConnectionsSheetResult.Failed(
                    error = StripeException(
                        message = "Financial Connections failed: $error",
                        stripeError = null,
                        statusCode = null,
                        requestId = null,
                        cause = null
                    )
                )
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            continuation.invokeOnCancellation {
                // Cleanup if coroutine is cancelled
            }
        }
    }

    /**
     * Present the Financial Connections Sheet and create a token.
     *
     * This uses Stripe.js collectBankAccountToken to present the Financial Connections flow
     * and create a token for the linked account.
     *
     * @return The result of the Financial Connections flow with token
     */
    public actual suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult {
        return suspendCancellableCoroutine { continuation ->
            val stripe = Stripe.getInstance()

            val stripeJs = stripe.stripeInstance?.unsafeCast<StripeInstanceFinancialConnections>()
            if (stripeJs == null) {
                val result = FinancialConnectionsSheetForTokenResult.Failed(
                    error = StripeException(
                        message = "Stripe.js not loaded. Call Stripe.initialize() and wait for load to complete.",
                        stripeError = null,
                        statusCode = null,
                        requestId = null,
                        cause = null
                    )
                )
                if (continuation.isActive) {
                    continuation.resume(result)
                }
                return@suspendCancellableCoroutine
            }

            // Collect bank account token using Financial Connections
            val options = object : StripeCollectBankAccountOptions {
                override var clientSecret = configuration.financialConnectionsSessionClientSecret
                override var params = object {
                    val payment_method_type = "us_bank_account"
                }
            }

            stripeJs.collectBankAccountToken(options).then { result ->
                val mappedResult = if (result.error != null) {
                    FinancialConnectionsSheetForTokenResult.Failed(
                        error = StripeException(
                            message = result.error!!.message,
                            stripeError = StripeError(
                                type = "api_error",
                                code = result.error!!.code,
                                message = result.error!!.message
                            ),
                            statusCode = null,
                            requestId = null,
                            cause = null
                        )
                    )
                } else {
                    // Note: Stripe.js doesn't directly return a token in collectBankAccountToken.
                    // The token would typically be created on your server using the account ID.
                    FinancialConnectionsSheetForTokenResult.Failed(
                        error = StripeException(
                            message = "Token creation not directly supported in Stripe.js. " +
                                    "Use the account from the session to create a token on your server.",
                            stripeError = null,
                            statusCode = null,
                            requestId = null,
                            cause = null
                        )
                    )
                }

                if (continuation.isActive) {
                    continuation.resume(mappedResult)
                }
            }.catch { error ->
                val result = FinancialConnectionsSheetForTokenResult.Failed(
                    error = StripeException(
                        message = "Financial Connections failed: $error",
                        stripeError = null,
                        statusCode = null,
                        requestId = null,
                        cause = null
                    )
                )
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            continuation.invokeOnCancellation {
                // Cleanup if coroutine is cancelled
            }
        }
    }

    private fun mapSession(jsSession: dynamic): FinancialConnectionsSession {
        val accounts = jsSession.accounts?.unsafeCast<Array<dynamic>>() ?: emptyArray()

        return FinancialConnectionsSession(
            id = jsSession.id?.toString() ?: "",
            clientSecret = jsSession.client_secret?.toString() ?: "",
            linkedAccounts = accounts.map { mapLinkedAccount(it) },
            livemode = jsSession.livemode == true,
            returnUrl = jsSession.return_url?.toString()
        )
    }

    private fun mapLinkedAccount(jsAccount: dynamic): FinancialConnectionsLinkedAccount {
        return FinancialConnectionsLinkedAccount(
            id = jsAccount.id?.toString() ?: "",
            institutionName = jsAccount.institution_name?.toString() ?: "Unknown",
            displayName = jsAccount.display_name?.toString(),
            last4 = jsAccount.last4?.toString(),
            created = (jsAccount.created as? Number)?.toLong() ?: 0L,
            balance = jsAccount.balance?.let { balanceData -> mapBalance(balanceData) },
            balanceRefresh = jsAccount.balance_refresh?.status?.toString()?.let { statusStr ->
                mapBalanceRefreshStatus(statusStr)
            },
            category = mapAccountCategory(jsAccount.category?.toString() ?: "other"),
            subcategory = jsAccount.subcategory?.toString()?.let { mapAccountSubcategory(it) },
            supportedPaymentMethodTypes = (jsAccount.supported_payment_method_types?.unsafeCast<Array<String>>()
                ?: emptyArray()).toList(),
            status = mapLinkedAccountStatus(jsAccount.status?.toString() ?: "active"),
            livemode = jsAccount.livemode == true
        )
    }

    private fun mapBalance(jsBalance: dynamic): FinancialConnectionsBalance {
        return FinancialConnectionsBalance(
            asOfDate = (jsBalance.as_of as? Number)?.toLong() ?: 0L,
            current = jsBalance.current?.let { currentBalance ->
                BalanceAmount(
                    amount = (currentBalance.amount as? Number)?.toLong() ?: 0L,
                    currency = currentBalance.currency?.toString() ?: "usd"
                )
            },
            available = jsBalance.available?.let { availableBalance ->
                BalanceAmount(
                    amount = (availableBalance.amount as? Number)?.toLong() ?: 0L,
                    currency = availableBalance.currency?.toString() ?: "usd"
                )
            },
            type = when (jsBalance.type?.toString()?.lowercase()) {
                "credit" -> BalanceType.CREDIT
                else -> BalanceType.CASH
            }
        )
    }

    private fun mapBalanceRefreshStatus(status: String): BalanceRefreshStatus {
        return when (status.lowercase()) {
            "pending" -> BalanceRefreshStatus.PENDING
            "succeeded" -> BalanceRefreshStatus.SUCCEEDED
            "failed" -> BalanceRefreshStatus.FAILED
            else -> BalanceRefreshStatus.PENDING
        }
    }

    private fun mapAccountCategory(category: String): AccountCategory {
        return when (category.lowercase()) {
            "cash" -> AccountCategory.CASH
            "credit" -> AccountCategory.CREDIT
            "investment" -> AccountCategory.INVESTMENT
            else -> AccountCategory.OTHER
        }
    }

    private fun mapAccountSubcategory(subcategory: String): AccountSubcategory {
        return when (subcategory.lowercase()) {
            "checking" -> AccountSubcategory.CHECKING
            "savings" -> AccountSubcategory.SAVINGS
            "credit_card" -> AccountSubcategory.CREDIT_CARD
            "line_of_credit" -> AccountSubcategory.LINE_OF_CREDIT
            "mortgage" -> AccountSubcategory.MORTGAGE
            else -> AccountSubcategory.OTHER
        }
    }

    private fun mapLinkedAccountStatus(status: String): LinkedAccountStatus {
        return when (status.lowercase()) {
            "active" -> LinkedAccountStatus.ACTIVE
            "inactive" -> LinkedAccountStatus.INACTIVE
            "disconnected" -> LinkedAccountStatus.DISCONNECTED
            else -> LinkedAccountStatus.ACTIVE
        }
    }
}
