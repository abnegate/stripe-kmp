package com.jakebarnby.stripe.model

/**
 * Customer object representing a customer of your business.
 * Customers allow you to perform recurring charges and track multiple charges associated with the same customer.
 *
 * @property id Unique identifier for the customer
 * @property email Customer's email address
 * @property name Customer's full name
 * @property phone Customer's phone number
 * @property description Description of the customer
 * @property created Creation timestamp
 * @property livemode Whether in live mode
 * @property defaultSource ID of the default payment source
 * @property shipping Shipping information
 * @property address Customer's address
 * @property balance Current balance if any
 * @property currency Three-letter ISO currency code
 * @property delinquent Whether the customer has a past due invoice
 * @property metadata Set of key-value pairs
 */
public data class Customer(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val created: Long,
    val livemode: Boolean,
    val defaultSource: String? = null,
    val shipping: ShippingDetails? = null,
    val address: Address? = null,
    val balance: Long? = null,
    val currency: String? = null,
    val delinquent: Boolean = false,
    val metadata: Map<String, String>? = null
) {
    init {
        require(id.isNotBlank()) { "Customer id cannot be blank" }
        require(created > 0) { "created timestamp must be positive" }
        currency?.let { require(it.length == 3) { "currency must be a three-letter ISO code" } }
        email?.let { require(it.contains("@")) { "email must be a valid email address" } }
    }
}

/**
 * CustomerSession object representing a session for customer-facing operations.
 * Customer sessions enable secure client-side access to customer data.
 *
 * @property id Unique identifier for the customer session
 * @property customerId ID of the customer this session belongs to
 * @property clientSecret Client secret for authenticating the session
 * @property expiresAt Timestamp when the session expires
 * @property livemode Whether in live mode
 */
public data class CustomerSession(
    val id: String,
    val customerId: String,
    val clientSecret: String,
    val expiresAt: Long,
    val livemode: Boolean
) {
    init {
        require(id.isNotBlank()) { "CustomerSession id cannot be blank" }
        require(customerId.isNotBlank()) { "customerId cannot be blank" }
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
        require(expiresAt > 0) { "expiresAt must be positive" }
    }

    /**
     * Check if the session has expired.
     * @param currentTimeMillis Current time in milliseconds
     */
    public fun isExpired(currentTimeMillis: Long): Boolean {
        // Convert expiresAt from seconds to milliseconds for comparison
        return currentTimeMillis >= (expiresAt * 1000)
    }
}

/**
 * EphemeralKey object representing a short-lived API key.
 * Ephemeral keys are used to authenticate customer sessions on the client side.
 *
 * @property id Unique identifier for the ephemeral key
 * @property created Creation timestamp
 * @property expires Expiration timestamp
 * @property livemode Whether in live mode
 * @property secret The ephemeral key secret (only available when first created)
 * @property associatedObjects Objects this key is associated with
 */
public data class EphemeralKey(
    val id: String,
    val created: Long,
    val expires: Long,
    val livemode: Boolean,
    val secret: String? = null,
    val associatedObjects: List<AssociatedObject> = emptyList()
) {
    init {
        require(id.isNotBlank()) { "EphemeralKey id cannot be blank" }
        require(created > 0) { "created timestamp must be positive" }
        require(expires > 0) { "expires timestamp must be positive" }
        require(expires > created) { "expires must be after created" }
    }

    /**
     * Override toString to redact sensitive secret.
     */
    override fun toString(): String {
        return "EphemeralKey(id='$id', created=$created, expires=$expires, livemode=$livemode, secret='***REDACTED***')"
    }

    /**
     * Check if the key has expired.
     * @param currentTimeMillis Current time in milliseconds
     */
    public fun isExpired(currentTimeMillis: Long): Boolean {
        // Convert expires from seconds to milliseconds for comparison
        return currentTimeMillis >= (expires * 1000)
    }

    /**
     * Object associated with an ephemeral key.
     *
     * @property id ID of the associated object
     * @property type Type of the associated object (e.g., "customer")
     */
    public data class AssociatedObject(
        val id: String,
        val type: String
    ) {
        init {
            require(id.isNotBlank()) { "AssociatedObject id cannot be blank" }
            require(type.isNotBlank()) { "AssociatedObject type cannot be blank" }
        }
    }
}

/**
 * Parameters for creating an ephemeral key.
 * Note: This is typically done server-side for security.
 *
 * @property customerId ID of the customer
 * @property stripeVersion Stripe API version to use
 */
public data class EphemeralKeyCreateParams(
    val customerId: String,
    val stripeVersion: String = "2024-12-18"
) {
    init {
        require(customerId.isNotBlank()) { "customerId cannot be blank" }
        require(stripeVersion.isNotBlank()) { "stripeVersion cannot be blank" }
    }
}
