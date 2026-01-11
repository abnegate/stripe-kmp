package com.jakebarnby.stripe

/**
 * Configuration for initializing the Stripe SDK.
 *
 * @property publishableKey Your Stripe publishable API key (must start with pk_test or pk_live)
 * @property merchantDisplayName The name of your business that will be displayed to customers
 * @property enableLogging Enable detailed logging for debugging (default: false)
 * @throws IllegalArgumentException if publishableKey doesn't start with pk_ or if it starts with sk_
 * @throws IllegalArgumentException if merchantDisplayName is blank
 */
public data class StripeConfiguration(
    val publishableKey: String,
    val merchantDisplayName: String,
    val enableLogging: Boolean = false
) {
    init {
        // CRITICAL-04: Reject secret keys
        require(!publishableKey.startsWith("sk_")) {
            "Invalid API key: Secret keys (starting with 'sk_') cannot be used. " +
            "Use a publishable key (starting with 'pk_test_' or 'pk_live_') instead."
        }

        // HIGH-01: Validate publishable key format
        require(publishableKey.startsWith("pk_test_") || publishableKey.startsWith("pk_live_")) {
            "Invalid publishable key format. Key must start with 'pk_test_' or 'pk_live_'."
        }

        // HIGH-01: Validate merchant name
        require(merchantDisplayName.isNotBlank()) {
            "Merchant display name cannot be blank"
        }
    }
}
