package fi.dvv.digiid.poc.domain.repository

import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.KeyInfo
import kotlinx.coroutines.flow.Flow
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager

interface ClientCertificateProvider {
    val keyInfo: Flow<KeyInfo>
    val keyManager: KeyManager
}

interface ProfileRepository : ClientCertificateProvider {
    val authState: Flow<AuthState>

    suspend fun setProfile(certificate: X509Certificate, pinCode: String)
    suspend fun unlock(pinCode: String)
    suspend fun logout()

    fun createSigningRequest(satu: String): String
    fun parseCertificate(pem: String): X509Certificate?
}