package fi.dvv.digiid.poc.data.repositories

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.KeyInfo
import fi.dvv.digiid.poc.domain.model.UserProfile
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.tls.HeldCertificate
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import timber.log.Timber
import java.io.StringWriter
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.inject.Inject
import javax.security.auth.x500.X500Principal

class ProfileRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val encryptedStorage: EncryptedStorageManager,
    testProfiles: Optional<List<UserProfile>>
) : ProfileRepository {
    override val availableProfiles: List<UserProfile> = testProfiles.orElse(emptyList())

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

    private val keyPair = MutableStateFlow(
        KeyStore.getInstance("AndroidKeyStore")?.let { keystore ->
            Timber.wtf("Attempting to load KeyPair from AndroidKeyStore")
            keystore.load(null)
            (keystore.getEntry(PRIVATE_KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.let {
                Timber.wtf("Found, wrapping it in a KeyPair")
                KeyPair(it.certificate.publicKey, it.privateKey)
            }
        } ?: run {
            Timber.wtf("Key not found, generating a new one…")
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                PRIVATE_KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                build()
            }

            kpg.initialize(parameterSpec)
            kpg.generateKeyPair()
        })

    override val keyInfo = keyPair.map {
        val keyInfo = KeyFactory.getInstance(
            it.private.algorithm,
            "AndroidKeyStore"
        ).getKeySpec(it.private, android.security.keystore.KeyInfo::class.java)

        KeyInfo(
            it.private.algorithm,
            keyInfo?.isInsideSecureHardware ?: false
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

    override suspend fun logout() {
        withContext(ioDispatcher) {
            encryptedStorage[KEY_PIN_CODE] = null
            encryptedStorage[KEY_USER_PROFILE] = null
            authState.emit(AuthState.Unauthenticated)
        }
    }

    override fun createSigningRequest(satu: String): String {
        val keyPair = this.keyPair.value

        val signer = JcaContentSignerBuilder("SHA256WITHECDSA").build(keyPair.private)
        val builder = JcaPKCS10CertificationRequestBuilder(
            X500Principal("C = FI, ST = Finland, L = Helsinki, O = DVV, CN = $satu"),
            keyPair.public
        )
        val extGen = ExtensionsGenerator().apply {
            addExtension(
                Extension.subjectAlternativeName,
                false,
                GeneralNames(GeneralName(GeneralName.dNSName, "URI:did:dvv:$satu"))
            )
        }
        builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate())
        val csr = builder.build(signer)

        return StringWriter().use { writer ->
            JcaPEMWriter(writer).use { pem ->
                pem.writeObject(csr)
            }

            writer.toString()
        }
    }

    override val clientCertificate: HeldCertificate?
        get() = when (val authState = authState.value) {
            is AuthState.Unlocked -> HeldCertificate.decode(authState.profile.certificatePEM)
            else -> null
        }

    companion object {
        const val KEY_PIN_CODE = "user_pin_code"
        const val KEY_USER_PROFILE = "user_profile"
        const val PRIVATE_KEY_ALIAS = "fi.dvv.digiid.UserProfile"
    }
}