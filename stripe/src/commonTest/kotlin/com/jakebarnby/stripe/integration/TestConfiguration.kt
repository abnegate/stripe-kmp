package com.jakebarnby.stripe.integration

/**
 * Test configuration for Stripe integration tests.
 * Uses Stripe test mode keys and test card numbers.
 */
public object TestConfiguration {
    /**
     * Override the publishable key for testing.
     * This allows tests to set a real API key when needed.
     */
    private var overrideKey: String? = null

    /**
     * Publishable key for integration tests.
     * Set via system property: -DSTRIPE_PUBLISHABLE_KEY=pk_test_xxx
     * Or use the placeholder for offline/mock tests.
     */
    public val publishableKey: String
        get() = overrideKey ?: "pk_test_placeholder"

    // Test card numbers from Stripe docs
    public object TestCards {
        public const val VISA_SUCCESS: String = "4242424242424242"
        public const val VISA_DECLINED: String = "4000000000000002"
        public const val VISA_REQUIRES_AUTH: String = "4000002500003155"
        public const val MASTERCARD: String = "5555555555554444"
        public const val AMEX: String = "378282246310005"
        public const val INVALID_LUHN: String = "4242424242424241"
    }

    // Test bank account numbers
    public object TestBankAccounts {
        public const val ROUTING_NUMBER: String = "110000000"
        public const val ACCOUNT_SUCCESS: String = "000123456789"
        public const val ACCOUNT_DECLINED: String = "000111111116"
    }

    /**
     * Check if real API tests should run.
     * This can be overridden by setting a real test API key.
     */
    public val shouldRunIntegrationTests: Boolean
        get() = publishableKey.startsWith("pk_test_") &&
                publishableKey != "pk_test_placeholder"

    /**
     * Set a real API key for integration tests.
     * Call this before running integration tests with a real Stripe API.
     */
    public fun setPublishableKey(key: String) {
        overrideKey = key
    }
}
