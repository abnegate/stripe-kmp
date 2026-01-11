package com.jakebarnby.stripe.model

/**
 * Physical address information.
 *
 * @property line1 First line of the address (e.g., street, PO Box, or company name)
 * @property line2 Second line of the address (e.g., apartment, suite, unit, or building)
 * @property city City, district, suburb, town, or village
 * @property state State, county, province, or region
 * @property postalCode ZIP or postal code
 * @property country Two-letter country code (ISO 3166-1 alpha-2)
 */
public data class Address(
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null
) {
    init {
        country?.let { countryCode ->
            require(countryCode.length == 2) {
                "Country code must be a two-letter ISO 3166-1 alpha-2 code, got: $countryCode"
            }
        }
    }

    /**
     * Builder for creating Address instances with a fluent API.
     */
    public class Builder {
        private var line1: String? = null
        private var line2: String? = null
        private var city: String? = null
        private var state: String? = null
        private var postalCode: String? = null
        private var country: String? = null

        public fun line1(line1: String?): Builder = apply { this.line1 = line1 }
        public fun line2(line2: String?): Builder = apply { this.line2 = line2 }
        public fun city(city: String?): Builder = apply { this.city = city }
        public fun state(state: String?): Builder = apply { this.state = state }
        public fun postalCode(postalCode: String?): Builder = apply { this.postalCode = postalCode }
        public fun country(country: String?): Builder = apply { this.country = country }

        public fun build(): Address = Address(
            line1 = line1,
            line2 = line2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country
        )
    }

    public companion object {
        /**
         * Create a builder for Address.
         */
        public fun builder(): Builder = Builder()
    }
}

/**
 * Billing details for a payment method or customer.
 *
 * @property name Full name
 * @property email Email address
 * @property phone Phone number
 * @property address Physical address
 */
public data class BillingDetails(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: Address? = null
) {
    init {
        email?.let { emailAddress ->
            require(emailAddress.contains("@")) {
                "Email must be a valid email address, got: $emailAddress"
            }
        }
    }

    /**
     * Builder for creating BillingDetails instances with a fluent API.
     */
    public class Builder {
        private var name: String? = null
        private var email: String? = null
        private var phone: String? = null
        private var address: Address? = null

        public fun name(name: String?): Builder = apply { this.name = name }
        public fun email(email: String?): Builder = apply { this.email = email }
        public fun phone(phone: String?): Builder = apply { this.phone = phone }
        public fun address(address: Address?): Builder = apply { this.address = address }

        public fun build(): BillingDetails = BillingDetails(
            name = name,
            email = email,
            phone = phone,
            address = address
        )
    }

    public companion object {
        /**
         * Create a builder for BillingDetails.
         */
        public fun builder(): Builder = Builder()
    }
}
