package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FinancialConnectionsSessionTest {
    @Test
    fun testValidSession() {
        val session = FinancialConnectionsSession(
            id = "fcsess_test_123",
            clientSecret = "fcsess_test_123_secret_abc",
            linkedAccounts = emptyList(),
            livemode = false,
            returnUrl = "https://example.com/return"
        )

        assertEquals("fcsess_test_123", session.id)
        assertEquals("fcsess_test_123_secret_abc", session.clientSecret)
        assertEquals(0, session.linkedAccounts.size)
        assertEquals(false, session.livemode)
        assertEquals("https://example.com/return", session.returnUrl)
    }

    @Test
    fun testSessionWithLinkedAccounts() {
        val account = FinancialConnectionsLinkedAccount(
            id = "fca_test_123",
            institutionName = "Test Bank",
            displayName = "Checking",
            last4 = "1234",
            created = 1234567890,
            balance = null,
            balanceRefresh = null,
            category = AccountCategory.CASH,
            subcategory = AccountSubcategory.CHECKING,
            supportedPaymentMethodTypes = listOf("us_bank_account"),
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )

        val session = FinancialConnectionsSession(
            id = "fcsess_test_123",
            clientSecret = "fcsess_test_123_secret_abc",
            linkedAccounts = listOf(account),
            livemode = false,
            returnUrl = null
        )

        assertEquals(1, session.linkedAccounts.size)
        assertEquals("fca_test_123", session.linkedAccounts[0].id)
    }

    @Test
    fun testBlankIdThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSession(
                id = "",
                clientSecret = "fcsess_test_123_secret_abc",
                linkedAccounts = emptyList(),
                livemode = false
            )
        }
    }

    @Test
    fun testBlankClientSecretThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSession(
                id = "fcsess_test_123",
                clientSecret = "",
                linkedAccounts = emptyList(),
                livemode = false
            )
        }
    }
}

class FinancialConnectionsLinkedAccountTest {
    @Test
    fun testValidLinkedAccount() {
        val account = FinancialConnectionsLinkedAccount(
            id = "fca_test_123",
            institutionName = "Test Bank",
            displayName = "Checking Account",
            last4 = "1234",
            created = 1234567890,
            balance = null,
            balanceRefresh = null,
            category = AccountCategory.CASH,
            subcategory = AccountSubcategory.CHECKING,
            supportedPaymentMethodTypes = listOf("us_bank_account", "ach_debit"),
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )

        assertEquals("fca_test_123", account.id)
        assertEquals("Test Bank", account.institutionName)
        assertEquals("Checking Account", account.displayName)
        assertEquals("1234", account.last4)
        assertEquals(1234567890, account.created)
        assertNull(account.balance)
        assertNull(account.balanceRefresh)
        assertEquals(AccountCategory.CASH, account.category)
        assertEquals(AccountSubcategory.CHECKING, account.subcategory)
        assertEquals(2, account.supportedPaymentMethodTypes.size)
        assertEquals(LinkedAccountStatus.ACTIVE, account.status)
        assertEquals(false, account.livemode)
    }

    @Test
    fun testLinkedAccountWithBalance() {
        val balance = FinancialConnectionsBalance(
            asOfDate = 1234567890,
            current = BalanceAmount(amount = 100000, currency = "usd"),
            available = BalanceAmount(amount = 95000, currency = "usd"),
            type = BalanceType.CASH
        )

        val account = FinancialConnectionsLinkedAccount(
            id = "fca_test_123",
            institutionName = "Test Bank",
            created = 1234567890,
            balance = balance,
            balanceRefresh = BalanceRefreshStatus.SUCCEEDED,
            category = AccountCategory.CASH,
            status = LinkedAccountStatus.ACTIVE,
            livemode = false
        )

        assertEquals(balance, account.balance)
        assertEquals(BalanceRefreshStatus.SUCCEEDED, account.balanceRefresh)
    }

    @Test
    fun testBlankIdThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsLinkedAccount(
                id = "",
                institutionName = "Test Bank",
                created = 1234567890,
                category = AccountCategory.CASH,
                status = LinkedAccountStatus.ACTIVE,
                livemode = false
            )
        }
    }

    @Test
    fun testBlankInstitutionNameThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsLinkedAccount(
                id = "fca_test_123",
                institutionName = "",
                created = 1234567890,
                category = AccountCategory.CASH,
                status = LinkedAccountStatus.ACTIVE,
                livemode = false
            )
        }
    }

    @Test
    fun testInvalidCreatedThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsLinkedAccount(
                id = "fca_test_123",
                institutionName = "Test Bank",
                created = 0,
                category = AccountCategory.CASH,
                status = LinkedAccountStatus.ACTIVE,
                livemode = false
            )
        }
    }

    @Test
    fun testInvalidLast4Throws() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsLinkedAccount(
                id = "fca_test_123",
                institutionName = "Test Bank",
                last4 = "123",
                created = 1234567890,
                category = AccountCategory.CASH,
                status = LinkedAccountStatus.ACTIVE,
                livemode = false
            )
        }
    }

    @Test
    fun testAllAccountCategories() {
        val categories = listOf(
            AccountCategory.CASH,
            AccountCategory.CREDIT,
            AccountCategory.INVESTMENT,
            AccountCategory.OTHER
        )

        assertEquals(4, categories.size)
    }

    @Test
    fun testAllAccountSubcategories() {
        val subcategories = listOf(
            AccountSubcategory.CHECKING,
            AccountSubcategory.SAVINGS,
            AccountSubcategory.CREDIT_CARD,
            AccountSubcategory.LINE_OF_CREDIT,
            AccountSubcategory.MORTGAGE,
            AccountSubcategory.OTHER
        )

        assertEquals(6, subcategories.size)
    }

    @Test
    fun testAllLinkedAccountStatuses() {
        val statuses = listOf(
            LinkedAccountStatus.ACTIVE,
            LinkedAccountStatus.INACTIVE,
            LinkedAccountStatus.DISCONNECTED
        )

        assertEquals(3, statuses.size)
    }
}

class FinancialConnectionsBalanceTest {
    @Test
    fun testValidBalance() {
        val balance = FinancialConnectionsBalance(
            asOfDate = 1234567890,
            current = BalanceAmount(amount = 100000, currency = "usd"),
            available = BalanceAmount(amount = 95000, currency = "usd"),
            type = BalanceType.CASH
        )

        assertEquals(1234567890, balance.asOfDate)
        assertEquals(100000, balance.current?.amount)
        assertEquals("usd", balance.current?.currency)
        assertEquals(95000, balance.available?.amount)
        assertEquals("usd", balance.available?.currency)
        assertEquals(BalanceType.CASH, balance.type)
    }

    @Test
    fun testBalanceWithNullAmounts() {
        val balance = FinancialConnectionsBalance(
            asOfDate = 1234567890,
            current = null,
            available = null,
            type = BalanceType.CASH
        )

        assertNull(balance.current)
        assertNull(balance.available)
    }

    @Test
    fun testInvalidAsOfDateThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsBalance(
                asOfDate = 0,
                type = BalanceType.CASH
            )
        }
    }

    @Test
    fun testAllBalanceTypes() {
        val types = listOf(
            BalanceType.CASH,
            BalanceType.CREDIT
        )

        assertEquals(2, types.size)
    }

    @Test
    fun testAllBalanceRefreshStatuses() {
        val statuses = listOf(
            BalanceRefreshStatus.PENDING,
            BalanceRefreshStatus.SUCCEEDED,
            BalanceRefreshStatus.FAILED
        )

        assertEquals(3, statuses.size)
    }
}

class BalanceAmountTest {
    @Test
    fun testValidBalanceAmount() {
        val amount = BalanceAmount(
            amount = 100000,
            currency = "usd"
        )

        assertEquals(100000, amount.amount)
        assertEquals("usd", amount.currency)
    }

    @Test
    fun testNegativeAmount() {
        val amount = BalanceAmount(
            amount = -5000,
            currency = "usd"
        )

        assertEquals(-5000, amount.amount)
    }

    @Test
    fun testInvalidCurrencyThrows() {
        assertFailsWith<IllegalArgumentException> {
            BalanceAmount(amount = 100000, currency = "us")
        }
    }

    @Test
    fun testVariousCurrencies() {
        val usd = BalanceAmount(amount = 100000, currency = "usd")
        val eur = BalanceAmount(amount = 100000, currency = "eur")
        val gbp = BalanceAmount(amount = 100000, currency = "gbp")

        assertEquals("usd", usd.currency)
        assertEquals("eur", eur.currency)
        assertEquals("gbp", gbp.currency)
    }
}

class FinancialConnectionsSheetConfigurationTest {
    @Test
    fun testValidConfiguration() {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_test_123_secret_abc",
            publishableKey = "pk_test_123"
        )

        assertEquals("fcsess_test_123_secret_abc", config.financialConnectionsSessionClientSecret)
        assertEquals("pk_test_123", config.publishableKey)
    }

    @Test
    fun testBlankClientSecretThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSheetConfiguration(
                financialConnectionsSessionClientSecret = "",
                publishableKey = "pk_test_123"
            )
        }
    }

    @Test
    fun testInvalidClientSecretFormatThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSheetConfiguration(
                financialConnectionsSessionClientSecret = "invalid_secret",
                publishableKey = "pk_test_123"
            )
        }
    }

    @Test
    fun testBlankPublishableKeyThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSheetConfiguration(
                financialConnectionsSessionClientSecret = "fcsess_test_123_secret_abc",
                publishableKey = ""
            )
        }
    }

    @Test
    fun testInvalidPublishableKeyThrows() {
        assertFailsWith<IllegalArgumentException> {
            FinancialConnectionsSheetConfiguration(
                financialConnectionsSessionClientSecret = "fcsess_test_123_secret_abc",
                publishableKey = "invalid_key"
            )
        }
    }

    @Test
    fun testLivePublishableKey() {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_live_123_secret_abc",
            publishableKey = "pk_live_123"
        )

        assertEquals("pk_live_123", config.publishableKey)
    }

    @Test
    fun testToStringRedactsClientSecret() {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_test_123_secret_abc",
            publishableKey = "pk_test_123"
        )

        val string = config.toString()
        kotlin.test.assertFalse(string.contains("fcsess_test_123_secret_abc"))
        kotlin.test.assertTrue(string.contains("REDACTED"))
        kotlin.test.assertTrue(string.contains("pk_test_123"))
    }

    @Test
    fun testToStringRedactsLivePublishableKey() {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_live_123_secret_abc",
            publishableKey = "pk_live_123"
        )

        val string = config.toString()
        kotlin.test.assertFalse(string.contains("fcsess_live_123_secret_abc"))
        kotlin.test.assertFalse(string.contains("pk_live_123"))
        kotlin.test.assertTrue(string.contains("REDACTED"))
    }
}

class FinancialConnectionsSheetResultTest {
    @Test
    fun testCompletedResult() {
        val session = FinancialConnectionsSession(
            id = "fcsess_test_123",
            clientSecret = "fcsess_test_123_secret_abc",
            linkedAccounts = emptyList(),
            livemode = false
        )

        val result = FinancialConnectionsSheetResult.Completed(session)

        kotlin.test.assertTrue(result is FinancialConnectionsSheetResult.Completed)
        assertEquals(session, result.session)
    }

    @Test
    fun testCanceledResult() {
        val result = FinancialConnectionsSheetResult.Canceled

        kotlin.test.assertTrue(result is FinancialConnectionsSheetResult.Canceled)
    }

    @Test
    fun testFailedResult() {
        val error = StripeException("Test error")
        val result = FinancialConnectionsSheetResult.Failed(error)

        kotlin.test.assertTrue(result is FinancialConnectionsSheetResult.Failed)
        assertEquals(error, result.error)
    }
}
