package com.jakebarnby.stripe.model

/**
 * Source object representing a payment source.
 * Sources are a legacy payment method type - prefer PaymentMethod for new integrations.
 *
 * @property id Unique identifier for the source
 * @property type Type of source (card, bancontact, ideal, etc.)
 * @property status Current status of the source
 * @property amount Amount associated with the source
 * @property currency Three-letter ISO currency code
 * @property clientSecret Client secret for the source
 * @property flow Authentication flow for the source
 * @property redirect Redirect information for the source
 * @property owner Owner information
 * @property created Creation timestamp
 * @property livemode Whether in live mode
 */
public data class Source(
    val id: String,
    val type: SourceType,
    val status: SourceStatus,
    val amount: Long? = null,
    val currency: String? = null,
    val clientSecret: String,
    val flow: SourceFlow,
    val redirect: SourceRedirect? = null,
    val owner: SourceOwner? = null,
    val created: Long,
    val livemode: Boolean
) {
    init {
        require(id.isNotBlank()) { "Source id cannot be blank" }
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
        currency?.let { require(it.length == 3) { "currency must be a three-letter ISO code" } }
    }

    /**
     * Override toString to redact sensitive clientSecret.
     */
    override fun toString(): String {
        return "Source(id='$id', clientSecret='***REDACTED***', type=$type, status=$status, created=$created, livemode=$livemode)"
    }
}

/**
 * Status of a Source object.
 */
public enum class SourceStatus(public val value: String) {
    /** Source is pending and requires additional action */
    PENDING("pending"),

    /** Source is chargeable and ready to use */
    CHARGEABLE("chargeable"),

    /** Source has been consumed and cannot be used again */
    CONSUMED("consumed"),

    /** Source was canceled */
    CANCELED("canceled"),

    /** Source failed */
    FAILED("failed");

    public companion object {
        public fun fromValue(value: String): SourceStatus? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Type of Source.
 */
public enum class SourceType(public val value: String) {
    CARD("card"),
    THREE_D_SECURE("three_d_secure"),
    GIROPAY("giropay"),
    SEPA_DEBIT("sepa_debit"),
    IDEAL("ideal"),
    SOFORT("sofort"),
    BANCONTACT("bancontact"),
    ALIPAY("alipay"),
    EPS("eps"),
    MULTIBANCO("multibanco"),
    P24("p24"),
    WECHAT("wechat"),
    UNKNOWN("unknown");

    public companion object {
        public fun fromValue(value: String): SourceType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

/**
 * Authentication flow for a Source.
 */
public enum class SourceFlow(public val value: String) {
    /** No authentication required */
    NONE("none"),

    /** Redirect-based authentication */
    REDIRECT("redirect"),

    /** Code verification required */
    CODE_VERIFICATION("code_verification"),

    /** Receiver-based flow */
    RECEIVER("receiver");

    public companion object {
        public fun fromValue(value: String): SourceFlow? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Redirect information for a Source.
 *
 * @property returnUrl URL to redirect the customer back to after authentication
 * @property status Status of the redirect
 * @property url URL to redirect the customer to for authentication
 */
public data class SourceRedirect(
    val returnUrl: String,
    val status: String? = null,
    val url: String? = null
) {
    init {
        require(returnUrl.isNotBlank()) { "returnUrl cannot be blank" }
    }
}

/**
 * Owner information for a Source.
 *
 * @property name Owner's name
 * @property email Owner's email
 * @property phone Owner's phone
 * @property address Owner's address
 * @property verifiedName Verified owner name
 * @property verifiedEmail Verified owner email
 * @property verifiedPhone Verified owner phone
 * @property verifiedAddress Verified owner address
 */
public data class SourceOwner(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: Address? = null,
    val verifiedName: String? = null,
    val verifiedEmail: String? = null,
    val verifiedPhone: String? = null,
    val verifiedAddress: Address? = null
)

/**
 * Parameters for creating a Source.
 * Use the companion object factory methods to create instances for specific source types.
 */
public data class SourceParams(
    val type: SourceType,
    val amount: Long? = null,
    val currency: String? = null,
    val owner: SourceOwner? = null,
    val redirect: SourceRedirect? = null,
    val metadata: Map<String, String>? = null,
    val extraParams: Map<String, Any>? = null
) {
    init {
        currency?.let { require(it.length == 3) { "currency must be a three-letter ISO code" } }
    }

    public companion object {
        /**
         * Create parameters for a card source.
         */
        public fun createCardParams(cardParams: CardParams): SourceParams {
            return SourceParams(
                type = SourceType.CARD,
                extraParams = mapOf(
                    "card" to mapOf(
                        "number" to cardParams.number,
                        "exp_month" to cardParams.expMonth,
                        "exp_year" to cardParams.expYear,
                        "cvc" to (cardParams.cvc ?: "")
                    )
                )
            )
        }

        /**
         * Create parameters for a Bancontact source.
         *
         * @param amount Amount in smallest currency unit
         * @param name Account holder name
         * @param returnUrl URL to redirect customer after authentication
         * @param statementDescriptor Statement descriptor
         * @param preferredLanguage Preferred language (en, de, fr, nl)
         */
        public fun createBancontactParams(
            amount: Long,
            name: String,
            returnUrl: String,
            statementDescriptor: String? = null,
            preferredLanguage: String? = null
        ): SourceParams {
            return SourceParams(
                type = SourceType.BANCONTACT,
                amount = amount,
                currency = "eur",
                owner = SourceOwner(name = name),
                redirect = SourceRedirect(returnUrl = returnUrl),
                extraParams = buildMap {
                    statementDescriptor?.let { put("statement_descriptor", it) }
                    preferredLanguage?.let { put("preferred_language", it) }
                }
            )
        }

        /**
         * Create parameters for an iDEAL source.
         *
         * @param amount Amount in smallest currency unit
         * @param name Account holder name
         * @param returnUrl URL to redirect customer after authentication
         * @param statementDescriptor Statement descriptor
         * @param bank Bank identifier (optional - if null, customer will choose)
         */
        public fun createIdealParams(
            amount: Long,
            name: String,
            returnUrl: String,
            statementDescriptor: String? = null,
            bank: String? = null
        ): SourceParams {
            return SourceParams(
                type = SourceType.IDEAL,
                amount = amount,
                currency = "eur",
                owner = SourceOwner(name = name),
                redirect = SourceRedirect(returnUrl = returnUrl),
                extraParams = buildMap {
                    statementDescriptor?.let { put("statement_descriptor", it) }
                    bank?.let { put("bank", it) }
                }
            )
        }

        /**
         * Create parameters for a Giropay source.
         *
         * @param amount Amount in smallest currency unit
         * @param name Account holder name
         * @param returnUrl URL to redirect customer after authentication
         * @param statementDescriptor Statement descriptor
         */
        public fun createGiropayParams(
            amount: Long,
            name: String,
            returnUrl: String,
            statementDescriptor: String? = null
        ): SourceParams {
            return SourceParams(
                type = SourceType.GIROPAY,
                amount = amount,
                currency = "eur",
                owner = SourceOwner(name = name),
                redirect = SourceRedirect(returnUrl = returnUrl),
                extraParams = statementDescriptor?.let { mapOf("statement_descriptor" to it) }
            )
        }

        /**
         * Create parameters for a SOFORT source.
         *
         * @param amount Amount in smallest currency unit
         * @param returnUrl URL to redirect customer after authentication
         * @param country Country code (DE, AT, BE, ES, IT, NL)
         * @param statementDescriptor Statement descriptor
         */
        public fun createSofortParams(
            amount: Long,
            returnUrl: String,
            country: String,
            statementDescriptor: String? = null
        ): SourceParams {
            return SourceParams(
                type = SourceType.SOFORT,
                amount = amount,
                currency = "eur",
                redirect = SourceRedirect(returnUrl = returnUrl),
                extraParams = buildMap {
                    put("country", country)
                    statementDescriptor?.let { put("statement_descriptor", it) }
                }
            )
        }

        /**
         * Create parameters for a SEPA debit source.
         *
         * @param name Account holder name
         * @param iban IBAN number
         * @param addressLine1 Address line 1
         * @param city City
         * @param postalCode Postal code
         * @param country Country code
         */
        public fun createSepaDebitParams(
            name: String,
            iban: String,
            addressLine1: String? = null,
            city: String? = null,
            postalCode: String? = null,
            country: String? = null
        ): SourceParams {
            return SourceParams(
                type = SourceType.SEPA_DEBIT,
                currency = "eur",
                owner = SourceOwner(
                    name = name,
                    address = Address(
                        line1 = addressLine1,
                        city = city,
                        postalCode = postalCode,
                        country = country
                    )
                ),
                extraParams = mapOf("iban" to iban)
            )
        }

        /**
         * Create parameters for an Alipay source.
         *
         * @param amount Amount in smallest currency unit
         * @param currency Currency code
         * @param returnUrl URL to redirect customer after authentication
         */
        public fun createAlipayParams(
            amount: Long,
            currency: String,
            returnUrl: String
        ): SourceParams {
            return SourceParams(
                type = SourceType.ALIPAY,
                amount = amount,
                currency = currency,
                redirect = SourceRedirect(returnUrl = returnUrl)
            )
        }

        /**
         * Create parameters for a P24 source.
         *
         * @param amount Amount in smallest currency unit
         * @param currency Currency code (must be EUR or PLN)
         * @param email Customer email
         * @param name Customer name
         * @param returnUrl URL to redirect customer after authentication
         */
        public fun createP24Params(
            amount: Long,
            currency: String,
            email: String,
            name: String? = null,
            returnUrl: String
        ): SourceParams {
            return SourceParams(
                type = SourceType.P24,
                amount = amount,
                currency = currency,
                owner = SourceOwner(
                    email = email,
                    name = name
                ),
                redirect = SourceRedirect(returnUrl = returnUrl)
            )
        }
    }
}
