package fi.dvv.digiid.poc.data.services

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.VerifiableCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nl.minvws.encoding.Base45

class VerificationServiceImpl(private val ioDispatcher: CoroutineDispatcher): VerificationService {
    private val mapper = CBORMapper().findAndRegisterModules()

    override suspend fun decodeCredential(credential: ExportedCredential): VerifiableCredential? {
        if (!credential.qrCode.startsWith("vcfi:", true)) return null

        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(ioDispatcher) {
            val data = Base45.getDecoder().decode(credential.qrCode.drop(5))
            mapper.readValue(data, VerifiableCredential::class.java)
        }
    }
}