package com.jakebarnby.stripe.model

/**
 * PaymentMethod object representing a customer's payment instrument.
 * PaymentMethods are the preferred way to handle payment instruments in Stripe.
 *
 * @property id Unique identifier for the payment method
 * @property type Type of payment method
 * @property created Creation timestamp
 * @property livemode Whether in live mode
 * @property billingDetails Billing details
 * @property card Card details if type is CARD
 * @property customer Customer ID if attached to a customer
 * @property metadata Set of key-value pairs
 */
public data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val created: Long,
    val livemode: Boolean,
    val billingDetails: BillingDetails? = null,
    val card: Card? = null,
    val customer: String? = null,
    val metadata: Map<String, String>? = null
) {
    init {
        require(id.isNotBlank()) { "PaymentMethod id cannot be blank" }
        require(created > 0) { "created timestamp must be positive" }
    }
}

/**
 * All supported PaymentMethod types across Stripe platforms.
 * Comprehensive list of 30+ payment methods.
 */
public enum class PaymentMethodType(public val value: String) {
    CARD("card"),
    CARD_PRESENT("card_present"),
    FPX("fpx"),
    IDEAL("ideal"),
    SEPA_DEBIT("sepa_debit"),
    AU_BECS_DEBIT("au_becs_debit"),
    BACS_DEBIT("bacs_debit"),
    BANCONTACT("bancontact"),
    EPS("eps"),
    GIROPAY("giropay"),
    GRABPAY("grabpay"),
    KLARNA("klarna"),
    OXXO("oxxo"),
    P24("p24"),
    SOFORT("sofort"),
    ALIPAY("alipay"),
    WECHAT_PAY("wechat_pay"),
    AFFIRM("affirm"),
    AFTERPAY_CLEARPAY("afterpay_clearpay"),
    AMAZON_PAY("amazon_pay"),
    BLIK("blik"),
    BOLETO("boleto"),
    CASHAPP("cashapp"),
    CUSTOMER_BALANCE("customer_balance"),
    KONBINI("konbini"),
    LINK("link"),
    MOBILEPAY("mobilepay"),
    PAYPAL("paypal"),
    PROMPTPAY("promptpay"),
    REVOLUT_PAY("revolut_pay"),
    SWISH("swish"),
    TWINT("twint"),
    US_BANK_ACCOUNT("us_bank_account"),
    ZIP("zip"),
    UNKNOWN("unknown");

    public companion object {
        public fun fromValue(value: String): PaymentMethodType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

/**
 * Card details for a PaymentMethod.
 *
 * @property brand Card brand
 * @property last4 Last 4 digits
 * @property expMonth Expiration month (1-12)
 * @property expYear Expiration year (4 digits)
 * @property funding Funding type
 * @property country Country code where card was issued
 * @property fingerprint Unique fingerprint for the card
 * @property checks Card verification checks
 * @property wallet Digital wallet info if applicable
 * @property threeDSecureUsage 3D Secure usage info
 * @property networks Available networks
 */
public data class Card(
    val brand: CardBrand,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val funding: CardFunding,
    val country: String? = null,
    val fingerprint: String? = null,
    val checks: CardChecks? = null,
    val wallet: CardWallet? = null,
    val threeDSecureUsage: ThreeDSecureUsage? = null,
    val networks: CardNetworks? = null
) {
    init {
        require(last4.length == 4) { "last4 must be exactly 4 digits" }
        require(expMonth in 1..12) { "expMonth must be between 1 and 12" }
        require(expYear >= 1000) { "expYear must be a 4-digit year" }
        country?.let { require(it.length == 2) { "country must be a two-letter ISO code" } }
    }
}

/**
 * Card brand enumeration.
 */
public enum class CardBrand(public val value: String, public val displayName: String) {
    VISA("visa", "Visa"),
    MASTERCARD("mastercard", "Mastercard"),
    AMERICAN_EXPRESS("amex", "American Express"),
    DISCOVER("discover", "Discover"),
    JCB("jcb", "JCB"),
    DINERS_CLUB("diners", "Diners Club"),
    UNION_PAY("unionpay", "UnionPay"),
    UNKNOWN("unknown", "Unknown");

    public companion object {
        public fun fromValue(value: String): CardBrand {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

/**
 * Card funding type.
 */
public enum class CardFunding(public val value: String) {
    CREDIT("credit"),
    DEBIT("debit"),
    PREPAID("prepaid"),
    UNKNOWN("unknown");

    public companion object {
        public fun fromValue(value: String): CardFunding {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

/**
 * Card verification checks results.
 *
 * @property addressLine1Check Result of address line 1 check
 * @property addressPostalCodeCheck Result of postal code check
 * @property cvcCheck Result of CVC check
 */
public data class CardChecks(
    val addressLine1Check: String? = null,
    val addressPostalCodeCheck: String? = null,
    val cvcCheck: String? = null
)

/**
 * Digital wallet information.
 *
 * @property type Type of wallet (apple_pay, google_pay, etc.)
 */
public data class CardWallet(
    val type: String
)

/**
 * 3D Secure usage information.
 *
 * @property supported Whether 3DS is supported
 */
public data class ThreeDSecureUsage(
    val supported: Boolean
)

/**
 * Available card networks.
 *
 * @property available List of available networks
 * @property preferred Preferred network if any
 */
public data class CardNetworks(
    val available: List<String>,
    val preferred: String? = null
)

/**
 * Parameters for creating a PaymentMethod.
 * Use the companion object factory methods to create instances for specific payment method types.
 */
public data class PaymentMethodCreateParams(
    val type: PaymentMethodType,
    val billingDetails: BillingDetails? = null,
    val card: CardPaymentMethodCreateParams? = null,
    val metadata: Map<String, String>? = null
) {
    init {
        when (type) {
            PaymentMethodType.CARD -> require(card != null) {
                "card details are required when type is CARD"
            }
            else -> {} // Other types would have their own validation
        }
    }

    /**
     * Builder for creating PaymentMethodCreateParams with a fluent API.
     */
    public class Builder {
        private var type: PaymentMethodType = PaymentMethodType.CARD
        private var billingDetails: BillingDetails? = null
        private var card: CardPaymentMethodCreateParams? = null
        private var metadata: Map<String, String>? = null

        public fun type(type: PaymentMethodType): Builder = apply { this.type = type }
        public fun billingDetails(billingDetails: BillingDetails?): Builder = apply { this.billingDetails = billingDetails }
        public fun card(card: CardPaymentMethodCreateParams?): Builder = apply { this.card = card }
        public fun metadata(metadata: Map<String, String>?): Builder = apply { this.metadata = metadata }

        public fun build(): PaymentMethodCreateParams = PaymentMethodCreateParams(
            type = type,
            billingDetails = billingDetails,
            card = card,
            metadata = metadata
        )
    }

    public companion object {
        /**
         * Create a builder for PaymentMethodCreateParams.
         */
        public fun builder(): Builder = Builder()

        /**
         * Create parameters for a card payment method.
         *
         * @param number Card number
         * @param expMonth Expiration month (1-12)
         * @param expYear Expiration year (2 or 4 digits)
         * @param cvc Card verification code
         * @param billingDetails Optional billing details
         */
        public fun createCard(
            number: String,
            expMonth: Int,
            expYear: Int,
            cvc: String? = null,
            billingDetails: BillingDetails? = null
        ): PaymentMethodCreateParams {
            return PaymentMethodCreateParams(
                type = PaymentMethodType.CARD,
                billingDetails = billingDetails,
                card = CardPaymentMethodCreateParams(
                    number = number,
                    expMonth = expMonth,
                    expYear = expYear,
                    cvc = cvc
                )
            )
        }

        /**
         * Create parameters for an iDEAL payment method.
         *
         * @param bank Optional bank identifier
         * @param billingDetails Billing details (required for iDEAL)
         */
        public fun createIdeal(
            bank: String? = null,
            billingDetails: BillingDetails
        ): PaymentMethodCreateParams {
            return PaymentMethodCreateParams(
                type = PaymentMethodType.IDEAL,
                billingDetails = billingDetails,
                metadata = bank?.let { mapOf("bank" to it) }
            )
        }

        /**
         * Create parameters for a SEPA debit payment method.
         *
         * @param iban IBAN number
         * @param billingDetails Billing details (required for SEPA)
         */
        public fun createSepaDebit(
            iban: String,
            billingDetails: BillingDetails
        ): PaymentMethodCreateParams {
            return PaymentMethodCreateParams(
                type = PaymentMethodType.SEPA_DEBIT,
                billingDetails = billingDetails,
                metadata = mapOf("iban" to iban)
            )
        }

        /**
         * Create parameters for a card payment method using a token.
         *
         * @param token Token ID (tok_xxx)
         * @param billingDetails Optional billing details
         */
        public fun createCardFromToken(
            token: String,
            billingDetails: BillingDetails? = null
        ): PaymentMethodCreateParams {
            return PaymentMethodCreateParams(
                type = PaymentMethodType.CARD,
                billingDetails = billingDetails,
                card = CardPaymentMethodCreateParams(token = token)
            )
        }
    }
}

/**
 * Card details for creating a PaymentMethod.
 *
 * @property number Card number
 * @property expMonth Expiration month (1-12)
 * @property expYear Expiration year (2 or 4 digits)
 * @property cvc Card verification code
 * @property token Token ID (alternative to providing card details directly)
 */
public data class CardPaymentMethodCreateParams(
    val number: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val cvc: String? = null,
    val token: String? = null
) {
    init {
        // Either provide card details or a token, not both
        val hasCardDetails = number != null && expMonth != null && expYear != null
        val hasToken = token != null
        require(hasCardDetails xor hasToken) {
            "Must provide either card details (number, expMonth, expYear) or token, not both"
        }

        if (hasCardDetails) {
            require(number!!.isNotBlank()) { "Card number cannot be blank" }
            require(expMonth!! in 1..12) { "expMonth must be between 1 and 12" }
            require(expYear!! > 0) { "expYear must be positive" }
            cvc?.let { require(it.length in 3..4) { "CVC must be 3 or 4 digits" } }
        }

        if (hasToken) {
            require(token!!.isNotBlank()) { "Token cannot be blank" }
        }
    }

    /**
     * Builder for CardPaymentMethodCreateParams.
     */
    public class Builder {
        private var number: String? = null
        private var expMonth: Int? = null
        private var expYear: Int? = null
        private var cvc: String? = null
        private var token: String? = null

        public fun number(number: String): Builder = apply { this.number = number }
        public fun expMonth(expMonth: Int): Builder = apply { this.expMonth = expMonth }
        public fun expYear(expYear: Int): Builder = apply { this.expYear = expYear }
        public fun cvc(cvc: String?): Builder = apply { this.cvc = cvc }
        public fun token(token: String): Builder = apply { this.token = token }

        public fun build(): CardPaymentMethodCreateParams = CardPaymentMethodCreateParams(
            number = number,
            expMonth = expMonth,
            expYear = expYear,
            cvc = cvc,
            token = token
        )
    }

    public companion object {
        /**
         * Create a builder for CardPaymentMethodCreateParams.
         */
        public fun builder(): Builder = Builder()
    }
}
