package fi.dvv.digiid.poc.data.services

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.data.network.WalletService
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.VerifiableCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import nl.minvws.encoding.Base45
import javax.inject.Inject

class VerificationServiceImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val walletService: WalletService,
): VerificationService {
    private val cborMapper = CBORMapper().findAndRegisterModules()
    private val jsonMapper = JsonMapper().findAndRegisterModules().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    override suspend fun decodeCredential(credential: ExportedCredential): VerifiableCredential? {
        if (!credential.qrCode.startsWith("vcfi:", true)) return null

        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(ioDispatcher) {
            val data = Base45.getDecoder().decode(credential.qrCode.drop(5))
            cborMapper.readValue(data, VerifiableCredential::class.java)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun verify(credential: VerifiableCredential) = coroutineScope {
        kotlin.runCatching {
            val issuerPEM = async {
                walletService.getVerificationMethod(credential.proof.verificationMethod.toString())
            }

            withContext(ioDispatcher) {
                val signature = credential.proof.jws
                credential.proof.jws = null
                val payload = jsonMapper.writeValueAsString(credential)
                val jwk = JWK.parseFromPEMEncodedObjects(issuerPEM.await())
                val verifier = ECDSAVerifier(jwk.toECKey())
                val header = JWSHeader.Builder(JWSAlgorithm.ES256).build()
                verifier.verify(header, payload.toByteArray(), Base64URL(signature))
            }
        }.getOrNull() ?: false
    }
}