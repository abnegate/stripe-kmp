package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.IdentityVerificationSheetConfiguration
import com.jakebarnby.stripe.model.IdentityVerificationSheetResult
import com.jakebarnby.stripe.model.StripeException

/**
 * JVM implementation of IdentityVerificationSheet.
 * Identity Verification UI is not available on JVM as it's a server-side target.
 */
public actual class IdentityVerificationSheet {
    public actual suspend fun present(configuration: IdentityVerificationSheetConfiguration): IdentityVerificationSheetResult =
        IdentityVerificationSheetResult.Failed(
            StripeException("Identity Verification UI is not available on JVM. Use Android, iOS, or JS target.")
        )
}
