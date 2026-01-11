package com.jakebarnby.stripe.model

/**
 * Google Pay configuration for payment processing.
 *
 * @property environment Google Pay environment (TEST or PRODUCTION)
 * @property merchantName Name shown to the user in Google Pay
 * @property merchantCountryCode Two-letter country code where the merchant is located
 * @property allowedCardNetworks List of card networks accepted by the merchant
 * @property allowedCardAuthMethods Authentication methods accepted for cards
 * @property billingAddressRequired Whether billing address is required from the user
 * @property shippingAddressRequired Whether shipping address is required from the user
 * @property emailRequired Whether email address is required from the user
 * @property existingPaymentMethodRequired Whether to require an existing payment method
 */
public data class GooglePayConfiguration(
    val environment: GooglePayEnvironment,
    val merchantName: String,
    val merchantCountryCode: String,
    val allowedCardNetworks: List<CardBrand> = listOf(
        CardBrand.VISA,
        CardBrand.MASTERCARD,
        CardBrand.AMERICAN_EXPRESS,
        CardBrand.DISCOVER
    ),
    val allowedCardAuthMethods: List<CardAuthMethod> = listOf(
        CardAuthMethod.PAN_ONLY,
        CardAuthMethod.CRYPTOGRAM_3DS
    ),
    val billingAddressRequired: Boolean = false,
    val shippingAddressRequired: Boolean = false,
    val emailRequired: Boolean = false,
    val existingPaymentMethodRequired: Boolean = false
) {
    init {
        require(merchantName.isNotBlank()) { "merchantName cannot be blank" }
        require(merchantCountryCode.length == 2) { "merchantCountryCode must be a two-letter ISO code" }
        require(allowedCardNetworks.isNotEmpty()) { "allowedCardNetworks cannot be empty" }
        require(allowedCardAuthMethods.isNotEmpty()) { "allowedCardAuthMethods cannot be empty" }
    }

    /**
     * Builder for creating GooglePayConfiguration instances with a fluent API.
     */
    public class Builder {
        private var environment: GooglePayEnvironment = GooglePayEnvironment.TEST
        private var merchantName: String = ""
        private var merchantCountryCode: String = ""
        private var allowedCardNetworks: List<CardBrand> = listOf(
            CardBrand.VISA,
            CardBrand.MASTERCARD,
            CardBrand.AMERICAN_EXPRESS,
            CardBrand.DISCOVER
        )
        private var allowedCardAuthMethods: List<CardAuthMethod> = listOf(
            CardAuthMethod.PAN_ONLY,
            CardAuthMethod.CRYPTOGRAM_3DS
        )
        private var billingAddressRequired: Boolean = false
        private var shippingAddressRequired: Boolean = false
        private var emailRequired: Boolean = false
        private var existingPaymentMethodRequired: Boolean = false

        public fun environment(environment: GooglePayEnvironment): Builder = apply { this.environment = environment }
        public fun merchantName(merchantName: String): Builder = apply { this.merchantName = merchantName }
        public fun merchantCountryCode(merchantCountryCode: String): Builder = apply { this.merchantCountryCode = merchantCountryCode }
        public fun allowedCardNetworks(allowedCardNetworks: List<CardBrand>): Builder = apply { this.allowedCardNetworks = allowedCardNetworks }
        public fun allowedCardAuthMethods(allowedCardAuthMethods: List<CardAuthMethod>): Builder = apply { this.allowedCardAuthMethods = allowedCardAuthMethods }
        public fun billingAddressRequired(billingAddressRequired: Boolean): Builder = apply { this.billingAddressRequired = billingAddressRequired }
        public fun shippingAddressRequired(shippingAddressRequired: Boolean): Builder = apply { this.shippingAddressRequired = shippingAddressRequired }
        public fun emailRequired(emailRequired: Boolean): Builder = apply { this.emailRequired = emailRequired }
        public fun existingPaymentMethodRequired(existingPaymentMethodRequired: Boolean): Builder = apply { this.existingPaymentMethodRequired = existingPaymentMethodRequired }

        public fun build(): GooglePayConfiguration = GooglePayConfiguration(
            environment = environment,
            merchantName = merchantName,
            merchantCountryCode = merchantCountryCode,
            allowedCardNetworks = allowedCardNetworks,
            allowedCardAuthMethods = allowedCardAuthMethods,
            billingAddressRequired = billingAddressRequired,
            shippingAddressRequired = shippingAddressRequired,
            emailRequired = emailRequired,
            existingPaymentMethodRequired = existingPaymentMethodRequired
        )
    }

    public companion object {
        /**
         * Create a builder for GooglePayConfiguration.
         */
        public fun builder(): Builder = Builder()
    }
}

/**
 * Google Pay environment enumeration.
 */
public enum class GooglePayEnvironment {
    /** Test environment for development */
    TEST,
    /** Production environment for live payments */
    PRODUCTION
}

/**
 * Card authentication methods supported by Google Pay.
 */
public enum class CardAuthMethod {
    /** Basic card information (PAN) only */
    PAN_ONLY,
    /** 3D Secure cryptogram authentication */
    CRYPTOGRAM_3DS
}

/**
 * Apple Pay configuration for payment processing.
 *
 * @property merchantIdentifier Apple Pay merchant identifier
 * @property merchantCountryCode Two-letter country code where the merchant is located
 * @property currencyCode Three-letter ISO currency code
 * @property supportedNetworks List of card networks supported by the merchant
 * @property merchantCapabilities Merchant capabilities for processing payments
 * @property requiredBillingContactFields Contact fields required for billing
 * @property requiredShippingContactFields Contact fields required for shipping
 */
public data class ApplePayConfiguration(
    val merchantIdentifier: String,
    val merchantCountryCode: String,
    val currencyCode: String,
    val supportedNetworks: List<CardBrand> = listOf(
        CardBrand.VISA,
        CardBrand.MASTERCARD,
        CardBrand.AMERICAN_EXPRESS
    ),
    val merchantCapabilities: List<ApplePayMerchantCapability> = listOf(
        ApplePayMerchantCapability.CAPABILITY_3DS
    ),
    val requiredBillingContactFields: List<ApplePayContactField> = emptyList(),
    val requiredShippingContactFields: List<ApplePayContactField> = emptyList()
) {
    init {
        require(merchantIdentifier.isNotBlank()) { "merchantIdentifier cannot be blank" }
        require(merchantCountryCode.length == 2) { "merchantCountryCode must be a two-letter ISO code" }
        require(currencyCode.length == 3) { "currencyCode must be a three-letter ISO code" }
        require(supportedNetworks.isNotEmpty()) { "supportedNetworks cannot be empty" }
        require(merchantCapabilities.isNotEmpty()) { "merchantCapabilities cannot be empty" }
    }

    /**
     * Builder for creating ApplePayConfiguration instances with a fluent API.
     */
    public class Builder {
        private var merchantIdentifier: String = ""
        private var merchantCountryCode: String = ""
        private var currencyCode: String = ""
        private var supportedNetworks: List<CardBrand> = listOf(
            CardBrand.VISA,
            CardBrand.MASTERCARD,
            CardBrand.AMERICAN_EXPRESS
        )
        private var merchantCapabilities: List<ApplePayMerchantCapability> = listOf(
            ApplePayMerchantCapability.CAPABILITY_3DS
        )
        private var requiredBillingContactFields: List<ApplePayContactField> = emptyList()
        private var requiredShippingContactFields: List<ApplePayContactField> = emptyList()

        public fun merchantIdentifier(merchantIdentifier: String): Builder = apply { this.merchantIdentifier = merchantIdentifier }
        public fun merchantCountryCode(merchantCountryCode: String): Builder = apply { this.merchantCountryCode = merchantCountryCode }
        public fun currencyCode(currencyCode: String): Builder = apply { this.currencyCode = currencyCode }
        public fun supportedNetworks(supportedNetworks: List<CardBrand>): Builder = apply { this.supportedNetworks = supportedNetworks }
        public fun merchantCapabilities(merchantCapabilities: List<ApplePayMerchantCapability>): Builder = apply { this.merchantCapabilities = merchantCapabilities }
        public fun requiredBillingContactFields(requiredBillingContactFields: List<ApplePayContactField>): Builder = apply { this.requiredBillingContactFields = requiredBillingContactFields }
        public fun requiredShippingContactFields(requiredShippingContactFields: List<ApplePayContactField>): Builder = apply { this.requiredShippingContactFields = requiredShippingContactFields }

        public fun build(): ApplePayConfiguration = ApplePayConfiguration(
            merchantIdentifier = merchantIdentifier,
            merchantCountryCode = merchantCountryCode,
            currencyCode = currencyCode,
            supportedNetworks = supportedNetworks,
            merchantCapabilities = merchantCapabilities,
            requiredBillingContactFields = requiredBillingContactFields,
            requiredShippingContactFields = requiredShippingContactFields
        )
    }

    public companion object {
        /**
         * Create a builder for ApplePayConfiguration.
         */
        public fun builder(): Builder = Builder()
    }
}

/**
 * Apple Pay merchant capabilities.
 */
public enum class ApplePayMerchantCapability {
    /** 3D Secure authentication capability */
    CAPABILITY_3DS,
    /** Credit card processing capability */
    CAPABILITY_CREDIT,
    /** Debit card processing capability */
    CAPABILITY_DEBIT,
    /** EMV payment capability */
    CAPABILITY_EMV
}

/**
 * Contact fields that can be required in Apple Pay.
 */
public enum class ApplePayContactField {
    /** Full name */
    NAME,
    /** Email address */
    EMAIL,
    /** Phone number */
    PHONE,
    /** Postal address */
    POSTAL_ADDRESS
}

/**
 * Payment request for wallet payments.
 *
 * @property amount Amount in smallest currency unit (e.g., cents for USD)
 * @property currencyCode Three-letter ISO currency code
 * @property label Label shown to the user (e.g., merchant name or item description)
 * @property countryCode Two-letter country code for the payment
 */
public data class WalletPaymentRequest(
    val amount: Long,
    val currencyCode: String,
    val label: String,
    val countryCode: String
) {
    init {
        require(amount >= 0) { "amount must be non-negative" }
        require(currencyCode.length == 3) { "currencyCode must be a three-letter ISO code" }
        require(label.isNotBlank()) { "label cannot be blank" }
        require(countryCode.length == 2) { "countryCode must be a two-letter ISO code" }
    }

    /**
     * Builder for creating WalletPaymentRequest instances with a fluent API.
     */
    public class Builder {
        private var amount: Long = 0
        private var currencyCode: String = ""
        private var label: String = ""
        private var countryCode: String = ""

        public fun amount(amount: Long): Builder = apply { this.amount = amount }
        public fun currencyCode(currencyCode: String): Builder = apply { this.currencyCode = currencyCode }
        public fun label(label: String): Builder = apply { this.label = label }
        public fun countryCode(countryCode: String): Builder = apply { this.countryCode = countryCode }

        public fun build(): WalletPaymentRequest = WalletPaymentRequest(
            amount = amount,
            currencyCode = currencyCode,
            label = label,
            countryCode = countryCode
        )
    }

    public companion object {
        /**
         * Create a builder for WalletPaymentRequest.
         */
        public fun builder(): Builder = Builder()
    }
}

/**
 * Result of a wallet payment operation.
 */
public sealed class WalletPaymentResult {
    /**
     * Payment was successful.
     *
     * @property paymentMethodId Stripe PaymentMethod ID created from the wallet payment
     * @property token Optional Stripe Token created from the wallet payment
     */
    public data class Success(
        val paymentMethodId: String,
        val token: Token? = null
    ) : WalletPaymentResult() {
        init {
            require(paymentMethodId.isNotBlank()) { "paymentMethodId cannot be blank" }
        }
    }

    /**
     * Payment was canceled by the user.
     */
    public data object Canceled : WalletPaymentResult()

    /**
     * Payment failed with an error.
     *
     * @property error The error that caused the failure
     */
    public data class Failed(val error: StripeException) : WalletPaymentResult()
}
