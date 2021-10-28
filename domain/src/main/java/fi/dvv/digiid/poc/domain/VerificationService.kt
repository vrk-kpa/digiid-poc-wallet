package fi.dvv.digiid.poc.domain

import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.VerifiableCredential

interface VerificationService {
    suspend fun decodeCredential(credential: ExportedCredential): VerifiableCredential?
    suspend fun verify(credential: VerifiableCredential): Boolean
}