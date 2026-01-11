package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.FinancialConnectionsLinkedAccount
import com.jakebarnby.stripe.model.FinancialConnectionsSheetConfiguration
import com.jakebarnby.stripe.model.FinancialConnectionsSheetResult
import com.jakebarnby.stripe.model.StripeException
import com.jakebarnby.stripe.model.Token

/**
 * Platform-specific Financial Connections Sheet interface.
 * This provides the UI for linking bank accounts via Stripe Financial Connections.
 *
 * Financial Connections allows users to securely link their bank accounts to your app,
 * enabling ACH payments and other bank-based payment methods.
 */
public expect class FinancialConnectionsSheet {
    public companion object {
        /**
         * Create a new Financial Connections Sheet instance.
         *
         * @param configuration The configuration for the sheet
         * @return A new FinancialConnectionsSheet instance
         */
        public fun create(configuration: FinancialConnectionsSheetConfiguration): FinancialConnectionsSheet
    }

    /**
     * Present the Financial Connections Sheet to the user.
     *
     * This will show the native UI for linking bank accounts.
     * The user can search for their bank, authenticate, and select accounts to link.
     *
     * @return The result of the Financial Connections flow
     */
    public suspend fun present(): FinancialConnectionsSheetResult

    /**
     * Present the Financial Connections Sheet and create a token.
     *
     * This is useful for creating a token that can be used to create a payment method
     * or attach to a customer for future payments.
     *
     * @return The result of the Financial Connections flow with token
     */
    public suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult
}

/**
 * Result of presenting the Financial Connections Sheet for token creation.
 */
public sealed class FinancialConnectionsSheetForTokenResult {
    /**
     * The user successfully completed the bank account linking and a token was created.
     *
     * @property linkedAccount The linked bank account
     * @property token The token representing the linked account
     */
    public data class Completed(
        val linkedAccount: FinancialConnectionsLinkedAccount,
        val token: Token
    ) : FinancialConnectionsSheetForTokenResult()

    /**
     * The user canceled the bank account linking.
     */
    public data object Canceled : FinancialConnectionsSheetForTokenResult()

    /**
     * The bank account linking failed with an error.
     *
     * @property error The error that occurred
     */
    public data class Failed(val error: StripeException) : FinancialConnectionsSheetForTokenResult()
}
