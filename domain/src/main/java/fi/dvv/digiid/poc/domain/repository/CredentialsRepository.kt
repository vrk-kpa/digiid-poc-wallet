package fi.dvv.digiid.poc.domain.repository

import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.Credential
import fi.dvv.digiid.poc.vc.VerifiablePresentation
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

interface CredentialsRepository {
    val coreIdentity: StateFlow<VerifiablePresentation?>

    suspend fun loadCoreIdentity()

    fun<T: Credential> exportCredential(type: KClass<T>): ExportedCredential?
}