package fi.dvv.digiid.poc.data.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.UserProfile
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.tls.HeldCertificate
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val encryptedStorage: EncryptedStorageManager,
) : ProfileRepository {
    override val availableProfiles = listOf(UserProfile.TEST_USER_1, UserProfile.TEST_USER_2)

    private val objectMapper = ObjectMapper().registerKotlinModule()

    override val authState = runBlocking {
        val pin = withContext(ioDispatcher) {
            encryptedStorage[KEY_PIN_CODE]
        }

        MutableStateFlow(
            when (pin) {
                null -> AuthState.Unauthenticated
                else -> AuthState.Locked()
            }
        )
    }

    override suspend fun unlock(pinCode: String) {
        withContext(ioDispatcher) {
            if (pinCode != encryptedStorage[KEY_PIN_CODE]) {
                authState.emit(AuthState.Locked(true))
                return@withContext
            }

            encryptedStorage[KEY_USER_PROFILE]?.let {
                kotlin.runCatching {
                    val profile = objectMapper.readValue(it, UserProfile::class.java)
                    authState.emit(AuthState.Unlocked(profile))
                }.onFailure {
                    authState.emit(AuthState.Unauthenticated)
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun setProfile(profile: UserProfile, pinCode: String) {
        withContext(ioDispatcher) {
            encryptedStorage[KEY_PIN_CODE] = pinCode
            encryptedStorage[KEY_USER_PROFILE] = objectMapper.writeValueAsString(profile)
            authState.emit(AuthState.Unlocked(profile))
        }
    }

    override val clientCertificate = authState.map {
        if (it is AuthState.Unlocked) HeldCertificate.decode(it.profile.certificatePEM)
        else null
    }

    companion object {
        const val KEY_PIN_CODE = "user_pin_code"
        const val KEY_USER_PROFILE = "user_profile"
    }
}