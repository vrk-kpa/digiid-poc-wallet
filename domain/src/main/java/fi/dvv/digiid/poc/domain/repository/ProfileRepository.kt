package fi.dvv.digiid.poc.domain.repository

import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import okhttp3.tls.HeldCertificate

interface ClientCertificateProvider {
    val clientCertificate: HeldCertificate?
}

interface ProfileRepository : ClientCertificateProvider {
    val availableProfiles: List<UserProfile>

    val authState: Flow<AuthState>

    suspend fun unlock(pinCode: String)
    suspend fun setProfile(profile: UserProfile, pinCode: String)
}