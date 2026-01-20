package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClientResultTypesTest {

    @Test
    fun paymentSheetResult_exposesErrorDetails() {
        val error = StripeError(message = "Failed", code = "card_declined")
        val failed = PaymentSheetResult.Failed(error)

        assertEquals("Failed", failed.error.message)
        assertEquals("card_declined", failed.error.code)
        assertTrue(PaymentSheetResult.Completed is PaymentSheetResult)
    }

    @Test
    fun financialConnectionsSheetForTokenResult_buildsCompleted() {
        val linkedAccount = FinancialConnectionsLinkedAccount(
            id = "fca_123",
            institutionName = "Test Bank",
            created = 1L,
            category = AccountCategory.CASH,
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )
        val token = Token(
            id = "tok_123",
            type = "bank_account",
            created = 1L,
            livemode = false,
            used = false
        )

        val completed = FinancialConnectionsSheetForTokenResult.Completed(
            linkedAccount = linkedAccount,
            token = token
        )

        assertEquals("fca_123", completed.linkedAccount.id)
        assertEquals("tok_123", completed.token.id)
    }
}
