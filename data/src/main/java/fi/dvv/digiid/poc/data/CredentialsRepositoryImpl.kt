package fi.dvv.digiid.poc.data

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.vc.Credential
import fi.dvv.digiid.poc.vc.VerifiablePresentation
import kotlinx.coroutines.flow.MutableStateFlow
import nl.minvws.encoding.Base45
import kotlin.reflect.KClass

class CredentialsRepositoryImpl(
    private val walletService: WalletService
) : CredentialsRepository {
    private val mapper = CBORMapper().findAndRegisterModules()

    override val coreIdentity = MutableStateFlow<VerifiablePresentation?>(null)

    override suspend fun authorize(pinCode: String) {
        coreIdentity.value = walletService.getCoreID(pinCode)
    }

    override fun <T : Credential> exportCredential(type: KClass<T>) = kotlin.runCatching {
        val credential = coreIdentity.value?.verifiableCredentials?.firstOrNull { it.credentialSubject::class == type }
        val data = mapper.writeValueAsBytes(credential)
        val base45 = Base45.getEncoder().encodeToString(data)
        "vcfi:$base45"
    }.getOrNull()
}