package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FinancialConnectionsSheetTest {
    @Test
    fun testCreateFinancialConnectionsSheet() {
        val configuration = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_test_123_secret_abc",
            publishableKey = "pk_test_123"
        )

        val sheet = FinancialConnectionsSheet.create(configuration)

        assertNotNull(sheet)
    }

    @Test
    fun testFinancialConnectionsSheetForTokenResult() {
        val account = FinancialConnectionsLinkedAccount(
            id = "fca_test_123",
            institutionName = "Test Bank",
            displayName = "Checking",
            last4 = "1234",
            created = 1234567890,
            category = AccountCategory.CASH,
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )

        val token = Token(
            id = "btok_test_123",
            type = "bank_account",
            created = 1234567890,
            livemode = false,
            used = false,
            card = null,
            bankAccount = BankAccountToken(
                id = "ba_test_123",
                country = "US",
                currency = "usd",
                last4 = "1234",
                bankName = "Test Bank",
                accountHolderName = "John Doe",
                accountHolderType = "individual",
                routingNumber = "110000000",
                status = "new"
            )
        )

        val result = FinancialConnectionsSheetForTokenResult.Completed(
            linkedAccount = account,
            token = token
        )

        kotlin.test.assertTrue(result is FinancialConnectionsSheetForTokenResult.Completed)
        assertEquals(account, result.linkedAccount)
        assertEquals(token, result.token)
    }

    @Test
    fun testFinancialConnectionsSheetForTokenResultCanceled() {
        val result = FinancialConnectionsSheetForTokenResult.Canceled

        kotlin.test.assertTrue(result is FinancialConnectionsSheetForTokenResult.Canceled)
    }

    @Test
    fun testFinancialConnectionsSheetForTokenResultFailed() {
        val error = StripeException("Token creation failed")
        val result = FinancialConnectionsSheetForTokenResult.Failed(error)

        kotlin.test.assertTrue(result is FinancialConnectionsSheetForTokenResult.Failed)
        assertEquals(error, result.error)
    }

    @Test
    fun testFinancialConnectionsSheetResultSealed() {
        // Test that all result types can be handled in a when expression
        val completedResult: FinancialConnectionsSheetResult = FinancialConnectionsSheetResult.Completed(
            session = FinancialConnectionsSession(
                id = "fcsess_test_123",
                clientSecret = "fcsess_test_123_secret_abc",
                linkedAccounts = emptyList(),
                livemode = false
            )
        )

        val canceledResult: FinancialConnectionsSheetResult = FinancialConnectionsSheetResult.Canceled

        val failedResult: FinancialConnectionsSheetResult = FinancialConnectionsSheetResult.Failed(
            error = StripeException("Error")
        )

        // All results should be assignable to the sealed class
        val results = listOf(completedResult, canceledResult, failedResult)
        assertEquals(3, results.size)
    }

    @Test
    fun testFinancialConnectionsSheetForTokenResultSealed() {
        // Test that all result types can be handled in a when expression
        val account = FinancialConnectionsLinkedAccount(
            id = "fca_test_123",
            institutionName = "Test Bank",
            created = 1234567890,
            category = AccountCategory.CASH,
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )

        val token = Token(
            id = "btok_test_123",
            type = "bank_account",
            created = 1234567890,
            livemode = false,
            used = false
        )

        val completedResult: FinancialConnectionsSheetForTokenResult =
            FinancialConnectionsSheetForTokenResult.Completed(
                linkedAccount = account,
                token = token
            )

        val canceledResult: FinancialConnectionsSheetForTokenResult =
            FinancialConnectionsSheetForTokenResult.Canceled

        val failedResult: FinancialConnectionsSheetForTokenResult =
            FinancialConnectionsSheetForTokenResult.Failed(
                error = StripeException("Error")
            )

        // All results should be assignable to the sealed class
        val results = listOf(completedResult, canceledResult, failedResult)
        assertEquals(3, results.size)
    }
}
