package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.FinancialConnectionsSheetConfiguration
import com.jakebarnby.stripe.model.FinancialConnectionsSheetResult
import com.jakebarnby.stripe.model.StripeException

/**
 * JVM implementation of FinancialConnectionsSheet.
 * Financial Connections UI is not available on JVM as it's a server-side target.
 */
public actual class FinancialConnectionsSheet private constructor() {
    public actual companion object {
        public actual fun create(configuration: FinancialConnectionsSheetConfiguration): FinancialConnectionsSheet {
            return FinancialConnectionsSheet()
        }
    }

    public actual suspend fun present(): FinancialConnectionsSheetResult =
        FinancialConnectionsSheetResult.Failed(
            StripeException("Financial Connections UI is not available on JVM. Use Android, iOS, or JS target.")
        )

    public actual suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult =
        FinancialConnectionsSheetForTokenResult.Failed(
            StripeException("Financial Connections UI is not available on JVM. Use Android, iOS, or JS target.")
        )
}
