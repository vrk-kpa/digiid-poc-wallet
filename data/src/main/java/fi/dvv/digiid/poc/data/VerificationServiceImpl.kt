package fi.dvv.digiid.poc.data

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.vc.VerifiableCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nl.minvws.encoding.Base45

class VerificationServiceImpl(private val ioDispatcher: CoroutineDispatcher): VerificationService {
    private val mapper = CBORMapper().findAndRegisterModules()

    override suspend fun decodeCredential(qrCode: String): VerifiableCredential? {
        if (!qrCode.startsWith("vcfi:", true)) return null

        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(ioDispatcher) {
            val data = Base45.getDecoder().decode(qrCode.drop(5))
            mapper.readValue(data, VerifiableCredential::class.java)
        }
    }
}