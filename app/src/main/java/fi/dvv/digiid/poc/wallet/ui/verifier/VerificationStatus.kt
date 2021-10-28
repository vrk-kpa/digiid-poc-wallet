package fi.dvv.digiid.poc.wallet.ui.verifier

import fi.dvv.digiid.poc.vc.Credential

sealed class VerificationStatus {
    object Loading : VerificationStatus()
    object Invalid : VerificationStatus()
    data class Valid(val credential: Credential) : VerificationStatus()
}