package com.jakebarnby.stripe.model

/**
 * Token object representing tokenized payment information.
 * Tokens are single-use and can be used to create charges or customers.
 *
 * @property id Unique identifier for the token
 * @property type Type of token (card, bank_account, pii, account)
 * @property created Creation timestamp (Unix timestamp in seconds)
 * @property livemode Whether this token was created in live mode
 * @property used Whether this token has been used
 * @property card Card details if type is "card"
 * @property bankAccount Bank account details if type is "bank_account"
 */
public data class Token(
    val id: String,
    val type: String,
    val created: Long,
    val livemode: Boolean,
    val used: Boolean,
    val card: CardToken? = null,
    val bankAccount: BankAccountToken? = null
) {
    init {
        require(id.isNotBlank()) { "Token id cannot be blank" }
        require(type.isNotBlank()) { "Token type cannot be blank" }
        require(created > 0) { "Token created timestamp must be positive" }
    }
}

/**
 * Card information within a token.
 *
 * @property id Unique identifier for the card
 * @property brand Card brand (visa, mastercard, amex, etc.)
 * @property last4 Last 4 digits of the card number
 * @property expMonth Expiration month (1-12)
 * @property expYear Expiration year (4 digits)
 * @property funding Card funding type (credit, debit, prepaid, unknown)
 * @property country Two-letter country code where the card was issued
 * @property name Cardholder name
 * @property addressLine1 Address line 1
 * @property addressLine2 Address line 2
 * @property addressCity City
 * @property addressState State/Province
 * @property addressZip ZIP/Postal code
 * @property addressCountry Country code
 * @property cvcCheck Result of CVC check
 * @property addressLine1Check Result of address line 1 check
 * @property addressZipCheck Result of ZIP code check
 */
public data class CardToken(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val funding: String? = null,
    val country: String? = null,
    val name: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressCity: String? = null,
    val addressState: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null,
    val cvcCheck: String? = null,
    val addressLine1Check: String? = null,
    val addressZipCheck: String? = null
) {
    init {
        require(last4.length == 4) { "last4 must be exactly 4 digits" }
        require(expMonth in 1..12) { "expMonth must be between 1 and 12" }
        require(expYear >= 1000) { "expYear must be a 4-digit year" }
    }
}

/**
 * Bank account information within a token.
 *
 * @property id Unique identifier for the bank account
 * @property country Country code of the bank account
 * @property currency Currency code
 * @property last4 Last 4 digits of the account number
 * @property bankName Name of the bank
 * @property accountHolderName Name of the account holder
 * @property accountHolderType Type of account holder (individual or company)
 * @property routingNumber Routing number
 * @property status Status of the bank account
 */
public data class BankAccountToken(
    val id: String,
    val country: String,
    val currency: String,
    val last4: String,
    val bankName: String? = null,
    val accountHolderName: String? = null,
    val accountHolderType: String? = null,
    val routingNumber: String? = null,
    val status: String? = null
) {
    init {
        require(last4.length == 4) { "last4 must be exactly 4 digits" }
        require(country.length == 2) { "country must be a two-letter ISO code" }
        require(currency.length == 3) { "currency must be a three-letter ISO code" }
    }
}

/**
 * Parameters for creating a card token.
 *
 * @property number Card number
 * @property expMonth Expiration month (1-12)
 * @property expYear Expiration year (2 or 4 digits)
 * @property cvc Card verification code
 * @property name Cardholder name
 * @property addressLine1 Address line 1
 * @property addressLine2 Address line 2
 * @property addressCity City
 * @property addressState State/Province
 * @property addressZip ZIP/Postal code
 * @property addressCountry Country code
 * @property currency Currency for the card (optional, for Stripe Connect)
 */
public data class CardParams(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String? = null,
    val name: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressCity: String? = null,
    val addressState: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null,
    val currency: String? = null
) {
    init {
        require(number.isNotBlank()) { "Card number cannot be blank" }

        // Sanitize card number by removing spaces and dashes
        val sanitizedNumber = number.replace(Regex("[\\s-]"), "")

        // Validate that sanitized number contains only digits
        require(sanitizedNumber.all { it.isDigit() }) {
            "Card number must contain only digits (after removing spaces and dashes)"
        }

        // Validate card number length (13-19 digits after sanitization)
        require(sanitizedNumber.length in 13..19) {
            "Card number must be between 13 and 19 digits, got ${sanitizedNumber.length}"
        }

        // Validate using Luhn algorithm
        require(isValidLuhn(sanitizedNumber)) {
            "Card number failed Luhn check - invalid card number"
        }

        require(expMonth in 1..12) { "expMonth must be between 1 and 12" }
        require(expYear > 0) { "expYear must be positive" }
        cvc?.let { require(it.length in 3..4) { "CVC must be 3 or 4 digits" } }
        addressCountry?.let { require(it.length == 2) { "addressCountry must be a two-letter ISO code" } }
        currency?.let { require(it.length == 3) { "currency must be a three-letter ISO code" } }
    }

    /**
     * Get the sanitized card number with spaces and dashes removed.
     */
    public fun getSanitizedNumber(): String = number.replace(Regex("[\\s-]"), "")

    /**
     * Builder for creating CardParams instances with a fluent API.
     */
    public class Builder {
        private var number: String = ""
        private var expMonth: Int = 0
        private var expYear: Int = 0
        private var cvc: String? = null
        private var name: String? = null
        private var addressLine1: String? = null
        private var addressLine2: String? = null
        private var addressCity: String? = null
        private var addressState: String? = null
        private var addressZip: String? = null
        private var addressCountry: String? = null
        private var currency: String? = null

        public fun number(number: String): Builder = apply { this.number = number }
        public fun expMonth(expMonth: Int): Builder = apply { this.expMonth = expMonth }
        public fun expYear(expYear: Int): Builder = apply { this.expYear = expYear }
        public fun cvc(cvc: String?): Builder = apply { this.cvc = cvc }
        public fun name(name: String?): Builder = apply { this.name = name }
        public fun addressLine1(addressLine1: String?): Builder = apply { this.addressLine1 = addressLine1 }
        public fun addressLine2(addressLine2: String?): Builder = apply { this.addressLine2 = addressLine2 }
        public fun addressCity(addressCity: String?): Builder = apply { this.addressCity = addressCity }
        public fun addressState(addressState: String?): Builder = apply { this.addressState = addressState }
        public fun addressZip(addressZip: String?): Builder = apply { this.addressZip = addressZip }
        public fun addressCountry(addressCountry: String?): Builder = apply { this.addressCountry = addressCountry }
        public fun currency(currency: String?): Builder = apply { this.currency = currency }

        public fun build(): CardParams = CardParams(
            number = number,
            expMonth = expMonth,
            expYear = expYear,
            cvc = cvc,
            name = name,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            addressCity = addressCity,
            addressState = addressState,
            addressZip = addressZip,
            addressCountry = addressCountry,
            currency = currency
        )
    }

    public companion object {
        /**
         * Create a builder for CardParams.
         */
        public fun builder(): Builder = Builder()

        /**
         * Validate a card number using the Luhn algorithm (mod 10 check).
         * @param cardNumber The sanitized card number (digits only)
         * @return true if valid, false otherwise
         */
        internal fun isValidLuhn(cardNumber: String): Boolean {
            if (cardNumber.isEmpty()) return false

            var sum = 0
            var alternate = false

            // Process digits from right to left
            for (i in cardNumber.length - 1 downTo 0) {
                var digit = cardNumber[i].digitToInt()

                if (alternate) {
                    digit *= 2
                    if (digit > 9) {
                        digit -= 9
                    }
                }

                sum += digit
                alternate = !alternate
            }

            return sum % 10 == 0
        }
    }
}

/**
 * Parameters for creating a bank account token.
 *
 * @property country Country code (2-letter ISO code)
 * @property currency Currency code (3-letter ISO code)
 * @property accountNumber Bank account number
 * @property routingNumber Bank routing number
 * @property accountHolderName Name of account holder
 * @property accountHolderType Type of account holder (individual or company)
 */
public data class BankAccountTokenParams(
    val country: String,
    val currency: String,
    val accountNumber: String,
    val routingNumber: String? = null,
    val accountHolderName: String? = null,
    val accountHolderType: AccountHolderType? = null
) {
    init {
        require(country.length == 2) { "country must be a two-letter ISO code" }
        require(currency.length == 3) { "currency must be a three-letter ISO code" }
        require(accountNumber.isNotBlank()) { "accountNumber cannot be blank" }
    }

    /**
     * Type of account holder.
     */
    public enum class AccountHolderType(public val value: String) {
        INDIVIDUAL("individual"),
        COMPANY("company");

        public companion object {
            public fun fromValue(value: String): AccountHolderType? {
                return entries.find { it.value == value }
            }
        }
    }

    /**
     * Builder for creating BankAccountTokenParams instances with a fluent API.
     */
    public class Builder {
        private var country: String = ""
        private var currency: String = ""
        private var accountNumber: String = ""
        private var routingNumber: String? = null
        private var accountHolderName: String? = null
        private var accountHolderType: AccountHolderType? = null

        public fun country(country: String): Builder = apply { this.country = country }
        public fun currency(currency: String): Builder = apply { this.currency = currency }
        public fun accountNumber(accountNumber: String): Builder = apply { this.accountNumber = accountNumber }
        public fun routingNumber(routingNumber: String?): Builder = apply { this.routingNumber = routingNumber }
        public fun accountHolderName(accountHolderName: String?): Builder = apply { this.accountHolderName = accountHolderName }
        public fun accountHolderType(accountHolderType: AccountHolderType?): Builder = apply { this.accountHolderType = accountHolderType }

        public fun build(): BankAccountTokenParams = BankAccountTokenParams(
            country = country,
            currency = currency,
            accountNumber = accountNumber,
            routingNumber = routingNumber,
            accountHolderName = accountHolderName,
            accountHolderType = accountHolderType
        )
    }

    public companion object {
        /**
         * Create a builder for BankAccountTokenParams.
         */
        public fun builder(): Builder = Builder()
    }
}

/**
 * Parameters for creating a PII (Personal Identifiable Information) token.
 * Used for identity verification purposes.
 *
 * @property personalIdNumber Personal identification number (e.g., SSN)
 */
public data class PiiTokenParams(
    val personalIdNumber: String
) {
    init {
        require(personalIdNumber.isNotBlank()) { "personalIdNumber cannot be blank" }
    }
}

/**
 * Parameters for creating an account token (Stripe Connect).
 * This is a simplified version - full implementation would include all Connect account fields.
 *
 * @property businessType Type of business (individual or company)
 * @property tosShownAndAccepted Whether the user accepted the terms of service
 */
public data class AccountParams(
    val businessType: BusinessType,
    val tosShownAndAccepted: Boolean = false
) {
    /**
     * Type of business entity.
     */
    public enum class BusinessType(public val value: String) {
        INDIVIDUAL("individual"),
        COMPANY("company");

        public companion object {
            public fun fromValue(value: String): BusinessType? {
                return entries.find { it.value == value }
            }
        }
    }
}
