package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AddressTest {
    @Test
    fun testAddressCreation() {
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 4B",
            city = "San Francisco",
            state = "CA",
            postalCode = "94102",
            country = "US"
        )

        assertEquals("123 Main St", address.line1)
        assertEquals("Apt 4B", address.line2)
        assertEquals("San Francisco", address.city)
        assertEquals("CA", address.state)
        assertEquals("94102", address.postalCode)
        assertEquals("US", address.country)
    }

    @Test
    fun testAddressWithNullFields() {
        val address = Address()

        assertNull(address.line1)
        assertNull(address.line2)
        assertNull(address.city)
        assertNull(address.state)
        assertNull(address.postalCode)
        assertNull(address.country)
    }

    @Test
    fun testAddressCountryValidation() {
        assertFailsWith<IllegalArgumentException> {
            Address(country = "USA")
        }
    }

    @Test
    fun testAddressBuilder() {
        val address = Address.builder()
            .line1("123 Main St")
            .city("San Francisco")
            .state("CA")
            .postalCode("94102")
            .country("US")
            .build()

        assertEquals("123 Main St", address.line1)
        assertEquals("San Francisco", address.city)
    }
}

class BillingDetailsTest {
    @Test
    fun testBillingDetailsCreation() {
        val address = Address(
            line1 = "123 Main St",
            city = "San Francisco",
            state = "CA",
            postalCode = "94102",
            country = "US"
        )

        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            address = address
        )

        assertEquals("John Doe", billingDetails.name)
        assertEquals("john@example.com", billingDetails.email)
        assertEquals("+1234567890", billingDetails.phone)
        assertEquals(address, billingDetails.address)
    }

    @Test
    fun testBillingDetailsEmailValidation() {
        assertFailsWith<IllegalArgumentException> {
            BillingDetails(email = "invalid-email")
        }
    }

    @Test
    fun testBillingDetailsBuilder() {
        val billingDetails = BillingDetails.builder()
            .name("Jane Doe")
            .email("jane@example.com")
            .build()

        assertEquals("Jane Doe", billingDetails.name)
        assertEquals("jane@example.com", billingDetails.email)
    }
}
