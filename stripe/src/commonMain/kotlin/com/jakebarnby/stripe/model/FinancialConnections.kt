package com.jakebarnby.stripe.model

/**
 * Financial Connections Session represents a session for linking bank accounts.
 *
 * @property id Unique identifier for the session
 * @property clientSecret Client secret for the session
 * @property linkedAccounts List of linked bank accounts
 * @property livemode Whether this session was created in live mode
 * @property returnUrl Return URL after authentication flow
 */
public data class FinancialConnectionsSession(
    val id: String,
    val clientSecret: String,
    val linkedAccounts: List<FinancialConnectionsLinkedAccount> = emptyList(),
    val livemode: Boolean,
    val returnUrl: String? = null
) {
    init {
        require(id.isNotBlank()) { "Session id cannot be blank" }
        require(clientSecret.isNotBlank()) { "Client secret cannot be blank" }
    }
}

/**
 * Linked bank account from Financial Connections.
 *
 * @property id Unique identifier for the account
 * @property institutionName Name of the financial institution
 * @property displayName Display name for the account
 * @property last4 Last 4 digits of the account number
 * @property created Creation timestamp (Unix timestamp in seconds)
 * @property balance Current balance information
 * @property balanceRefresh Status of balance refresh operation
 * @property category Account category (cash, credit, investment, other)
 * @property subcategory Account subcategory (checking, savings, etc.)
 * @property supportedPaymentMethodTypes List of supported payment method types
 * @property status Account status (active, inactive, disconnected)
 * @property livemode Whether this account was created in live mode
 */
public data class FinancialConnectionsLinkedAccount(
    val id: String,
    val institutionName: String,
    val displayName: String? = null,
    val last4: String? = null,
    val created: Long,
    val balance: FinancialConnectionsBalance? = null,
    val balanceRefresh: BalanceRefreshStatus? = null,
    val category: AccountCategory,
    val subcategory: AccountSubcategory? = null,
    val supportedPaymentMethodTypes: List<String> = emptyList(),
    val status: LinkedAccountStatus,
    val livemode: Boolean
) {
    init {
        require(id.isNotBlank()) { "Account id cannot be blank" }
        require(institutionName.isNotBlank()) { "Institution name cannot be blank" }
        require(created > 0) { "Created timestamp must be positive" }
        last4?.let { require(it.length == 4) { "last4 must be exactly 4 characters" } }
    }
}

/**
 * Category of financial account.
 */
public enum class AccountCategory {
    CASH,
    CREDIT,
    INVESTMENT,
    OTHER
}

/**
 * Subcategory of financial account providing more specific type information.
 */
public enum class AccountSubcategory {
    CHECKING,
    SAVINGS,
    CREDIT_CARD,
    LINE_OF_CREDIT,
    MORTGAGE,
    OTHER
}

/**
 * Status of a linked financial account.
 */
public enum class LinkedAccountStatus {
    ACTIVE,
    INACTIVE,
    DISCONNECTED
}

/**
 * Balance information for a financial account.
 *
 * @property asOfDate Timestamp when balance was retrieved (Unix timestamp in seconds)
 * @property current Current balance
 * @property available Available balance
 * @property type Type of balance (cash or credit)
 */
public data class FinancialConnectionsBalance(
    val asOfDate: Long,
    val current: BalanceAmount? = null,
    val available: BalanceAmount? = null,
    val type: BalanceType
) {
    init {
        require(asOfDate > 0) { "asOfDate timestamp must be positive" }
    }
}

/**
 * Amount with currency for balance information.
 *
 * @property amount Amount in smallest currency unit (e.g., cents for USD)
 * @property currency Three-letter ISO currency code
 */
public data class BalanceAmount(
    val amount: Long,
    val currency: String
) {
    init {
        require(currency.length == 3) { "currency must be a three-letter ISO code" }
    }
}

/**
 * Type of balance (cash or credit).
 */
public enum class BalanceType {
    CASH,
    CREDIT
}

/**
 * Status of balance refresh operation.
 */
public enum class BalanceRefreshStatus {
    PENDING,
    SUCCEEDED,
    FAILED
}

/**
 * Configuration for presenting the Financial Connections Sheet.
 *
 * @property financialConnectionsSessionClientSecret Client secret from Financial Connections Session
 * @property publishableKey Stripe publishable key
 */
public data class FinancialConnectionsSheetConfiguration(
    val financialConnectionsSessionClientSecret: String,
    val publishableKey: String
) {
    init {
        require(financialConnectionsSessionClientSecret.isNotBlank()) {
            "Financial connections session client secret cannot be blank"
        }
        require(financialConnectionsSessionClientSecret.matches(
            Regex("^fcsess_[a-zA-Z0-9_]+_secret_[a-zA-Z0-9_]+$")
        )) {
            "Invalid Financial Connections session client secret format. " +
            "Expected format: 'fcsess_xxx_secret_xxx'."
        }
        require(publishableKey.isNotBlank()) { "Publishable key cannot be blank" }
        require(publishableKey.startsWith("pk_test_") || publishableKey.startsWith("pk_live_")) {
            "Publishable key must start with 'pk_test_' or 'pk_live_'"
        }
    }

    /**
     * Override toString to prevent client secret from being exposed in logs.
     */
    override fun toString(): String {
        return "FinancialConnectionsSheetConfiguration(" +
            "financialConnectionsSessionClientSecret=***REDACTED***, " +
            "publishableKey=${if (publishableKey.startsWith("pk_test_")) publishableKey else "***REDACTED***"})"
    }
}

/**
 * Result of presenting the Financial Connections Sheet.
 */
public sealed class FinancialConnectionsSheetResult {
    /**
     * The user successfully completed the bank account linking.
     *
     * @property session The Financial Connections Session with linked accounts
     */
    public data class Completed(val session: FinancialConnectionsSession) : FinancialConnectionsSheetResult()

    /**
     * The user canceled the bank account linking.
     */
    public data object Canceled : FinancialConnectionsSheetResult()

    /**
     * The bank account linking failed with an error.
     *
     * @property error The error that occurred
     */
    public data class Failed(val error: StripeException) : FinancialConnectionsSheetResult()
}
