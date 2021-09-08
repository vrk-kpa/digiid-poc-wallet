package fi.dvv.digiid.poc.domain

import fi.dvv.digiid.poc.vc.VerifiableCredential

interface VerificationService {
    suspend fun decodeCredential(qrCode: String): VerifiableCredential?
}