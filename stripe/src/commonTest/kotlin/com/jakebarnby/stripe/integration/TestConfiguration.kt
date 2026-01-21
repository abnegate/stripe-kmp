package com.jakebarnby.stripe.integration

/**
 * Test configuration for Stripe integration tests.
 * Uses Stripe test mode keys and test card numbers.
 *
 * To run integration tests with real API:
 * - JVM: Set system property -DSTRIPE_PUBLISHABLE_KEY=pk_test_xxx
 *        or environment variable STRIPE_PUBLISHABLE_KEY=pk_test_xxx
 * - Other platforms: Call TestConfiguration.setPublishableKey("pk_test_xxx") before tests
 */
public object TestConfiguration {
    /**
     * Override the publishable key for testing.
     * This allows tests to set a real API key when needed.
     */
    private var overrideKey: String? = null

    /**
     * Publishable key for integration tests.
     * Checks in order: override key, system property, placeholder
     */
    public val publishableKey: String
        get() = overrideKey
            ?: getSystemProperty("STRIPE_PUBLISHABLE_KEY")
            ?: getEnvironmentVariable("STRIPE_PUBLISHABLE_KEY")
            ?: "pk_test_placeholder"

    // Test card numbers from Stripe docs
    // https://stripe.com/docs/testing#cards
    public object TestCards {
        /** Visa - succeeds and immediately processes the payment */
        public const val VISA_SUCCESS: String = "4242424242424242"

        /** Visa - charge is declined with a generic_decline error */
        public const val VISA_DECLINED: String = "4000000000000002"

        /** Visa - requires authentication (3D Secure) */
        public const val VISA_REQUIRES_AUTH: String = "4000002500003155"

        /** Visa - insufficient funds decline */
        public const val VISA_INSUFFICIENT_FUNDS: String = "4000000000009995"

        /** Visa - lost card decline */
        public const val VISA_LOST_CARD: String = "4000000000009987"

        /** Visa - expired card decline */
        public const val VISA_EXPIRED_CARD: String = "4000000000000069"

        /** Visa - incorrect CVC decline */
        public const val VISA_INCORRECT_CVC: String = "4000000000000127"

        /** Visa - processing error */
        public const val VISA_PROCESSING_ERROR: String = "4000000000000119"

        /** Mastercard - succeeds */
        public const val MASTERCARD: String = "5555555555554444"

        /** American Express - succeeds */
        public const val AMEX: String = "378282246310005"

        /** Discover - succeeds */
        public const val DISCOVER: String = "6011111111111117"

        /** JCB - succeeds */
        public const val JCB: String = "3530111333300000"

        /** Diners Club - succeeds */
        public const val DINERS_CLUB: String = "3056930009020004"

        /** UnionPay - succeeds */
        public const val UNIONPAY: String = "6200000000000005"

        /** Invalid card number (fails Luhn check) */
        public const val INVALID_LUHN: String = "4242424242424241"

        /** Card that will produce an API error */
        public const val API_ERROR: String = "4000000000000099"
    }

    // Test bank account numbers from Stripe docs
    // https://stripe.com/docs/testing#bank-accounts
    public object TestBankAccounts {
        /** US routing number for testing */
        public const val ROUTING_NUMBER: String = "110000000"

        /** Account that succeeds */
        public const val ACCOUNT_SUCCESS: String = "000123456789"

        /** Account that will fail verification */
        public const val ACCOUNT_DECLINED: String = "000111111116"

        /** Account that will be verified then closed */
        public const val ACCOUNT_CLOSED: String = "000111111113"

        /** Account that verification will fail */
        public const val ACCOUNT_VERIFICATION_FAILED: String = "000000000009"
    }

    public object TestIBANs {
        /** Germany - succeeds */
        public const val GERMANY_SUCCESS: String = "DE89370400440532013000"

        /** France - succeeds */
        public const val FRANCE_SUCCESS: String = "FR1420041010050500013M02606"

        /** Netherlands - succeeds */
        public const val NETHERLANDS_SUCCESS: String = "NL91ABNA0417164300"
    }

    /**
     * Check if real API tests should run.
     * Returns true only if a real test API key is configured.
     */
    public val shouldRunIntegrationTests: Boolean
        get() = publishableKey.startsWith("pk_test_") &&
                publishableKey != "pk_test_placeholder"

    /**
     * Check if the configured key is a live key (should never be used in tests!)
     */
    public val isLiveKey: Boolean
        get() = publishableKey.startsWith("pk_live_")

    /**
     * Set a real API key for integration tests.
     * Call this before running integration tests with a real Stripe API.
     *
     * @param key The Stripe publishable key (must start with pk_test_)
     * @throws IllegalArgumentException if a live key is provided
     */
    public fun setPublishableKey(key: String) {
        require(!key.startsWith("pk_live_")) {
            "NEVER use a live API key for tests! Use pk_test_ keys only."
        }
        require(!key.startsWith("sk_")) {
            "Use a publishable key (pk_test_), not a secret key (sk_)."
        }
        overrideKey = key
    }

    /**
     * Clear any override key and revert to default behavior.
     */
    public fun clearPublishableKey() {
        overrideKey = null
    }

    /**
     * Get system property - platform-specific implementation
     */
    private fun getSystemProperty(name: String): String? {
        return platformGetSystemProperty(name)
    }

    /**
     * Get environment variable - platform-specific implementation
     */
    private fun getEnvironmentVariable(name: String): String? {
        return platformGetEnvironmentVariable(name)
    }
}

internal expect fun platformGetSystemProperty(name: String): String?

internal expect fun platformGetEnvironmentVariable(name: String): String?

/**
 * Check if running on JVM platform.
 * Returns true for JVM, false for other platforms (JS, iOS, Android, etc.)
 */
internal expect fun isJvmPlatform(): Boolean
