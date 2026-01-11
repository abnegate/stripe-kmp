package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomerTest {
    @Test
    fun testCustomerCreation() {
        val customer = Customer(
            id = "cus_123",
            email = "customer@example.com",
            name = "John Doe",
            created = 1234567890,
            livemode = false
        )

        assertEquals("cus_123", customer.id)
        assertEquals("customer@example.com", customer.email)
        assertEquals("John Doe", customer.name)
    }

    @Test
    fun testCustomerValidation() {
        assertFailsWith<IllegalArgumentException> {
            Customer(
                id = "",
                created = 1234567890,
                livemode = false
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Customer(
                id = "cus_123",
                email = "invalid-email",
                created = 1234567890,
                livemode = false
            )
        }
    }
}

class CustomerSessionTest {
    @Test
    fun testCustomerSessionCreation() {
        val session = CustomerSession(
            id = "cuss_123",
            customerId = "cus_123",
            clientSecret = "cuss_123_secret_abc",
            expiresAt = 1234567890,
            livemode = false
        )

        assertEquals("cuss_123", session.id)
        assertEquals("cus_123", session.customerId)
        assertEquals("cuss_123_secret_abc", session.clientSecret)
    }

    @Test
    fun testCustomerSessionIsExpired() {
        val currentTime = 1700000000000L // Fixed timestamp in milliseconds
        val futureTime = (currentTime / 1000) + 3600 // 1 hour from now in seconds
        val pastTime = (currentTime / 1000) - 3600 // 1 hour ago in seconds

        val validSession = CustomerSession(
            id = "cuss_123",
            customerId = "cus_123",
            clientSecret = "cuss_123_secret_abc",
            expiresAt = futureTime,
            livemode = false
        )

        val expiredSession = CustomerSession(
            id = "cuss_456",
            customerId = "cus_456",
            clientSecret = "cuss_456_secret_xyz",
            expiresAt = pastTime,
            livemode = false
        )

        assertFalse(validSession.isExpired(currentTime))
        assertTrue(expiredSession.isExpired(currentTime))
    }
}

class EphemeralKeyTest {
    @Test
    fun testEphemeralKeyCreation() {
        val key = EphemeralKey(
            id = "ephkey_123",
            created = 1234567890,
            expires = 1234571490,
            livemode = false,
            secret = "ephkey_123_secret_abc"
        )

        assertEquals("ephkey_123", key.id)
        assertEquals("ephkey_123_secret_abc", key.secret)
    }

    @Test
    fun testEphemeralKeyValidation() {
        assertFailsWith<IllegalArgumentException> {
            EphemeralKey(
                id = "ephkey_123",
                created = 1234567890,
                expires = 1234560000, // Before created
                livemode = false
            )
        }
    }

    @Test
    fun testEphemeralKeyIsExpired() {
        val currentTime = 1700000000000L // Fixed timestamp in milliseconds
        val now = currentTime / 1000 // in seconds
        val futureTime = now + 3600
        val pastTime = now - 3600

        val validKey = EphemeralKey(
            id = "ephkey_123",
            created = now,
            expires = futureTime,
            livemode = false
        )

        val expiredKey = EphemeralKey(
            id = "ephkey_456",
            created = pastTime - 3600,
            expires = pastTime,
            livemode = false
        )

        assertFalse(validKey.isExpired(currentTime))
        assertTrue(expiredKey.isExpired(currentTime))
    }

    @Test
    fun testAssociatedObject() {
        val associatedObject = EphemeralKey.AssociatedObject(
            id = "cus_123",
            type = "customer"
        )

        assertEquals("cus_123", associatedObject.id)
        assertEquals("customer", associatedObject.type)
    }
}
