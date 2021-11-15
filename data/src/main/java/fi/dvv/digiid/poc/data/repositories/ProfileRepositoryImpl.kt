package fi.dvv.digiid.poc.data.repositories

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.KeyInfo
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import timber.log.Timber
import java.io.StringReader
import java.io.StringWriter
import java.net.Socket
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import javax.inject.Inject
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.X509KeyManager
import javax.security.auth.x500.X500Principal

class ProfileRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val encryptedStorage: EncryptedStorageManager,
) : ProfileRepository {
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

    private val keystore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    override val keyManager: KeyManager = run {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keystore, null)
        val orig = kmf.keyManagers.first() as X509KeyManager

        object : X509KeyManager by orig {
            override fun chooseClientAlias(
                keyType: Array<out String>?,
                issuers: Array<out Principal>?,
                socket: Socket?
            ) = PRIVATE_KEY_ALIAS
        }
    }

    private val keyPair = MutableStateFlow(
        (keystore.getEntry(PRIVATE_KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.let {
            Timber.wtf("Found, wrapping it in a KeyPair")
            KeyPair(it.certificate.publicKey, it.privateKey)
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
                setDigests(
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512,
                    KeyProperties.DIGEST_NONE
                )

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

            authState.emit(AuthState.Unlocked)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun setProfile(certificate: X509Certificate, pinCode: String) {
        withContext(ioDispatcher) {
            encryptedStorage[KEY_PIN_CODE] = pinCode

            keystore.setKeyEntry(
                PRIVATE_KEY_ALIAS,
                keyPair.value.private,
                charArrayOf(),
                arrayOf(certificate)
            )

            keyPair.value = KeyPair(certificate.publicKey, keyPair.value.private)
            authState.emit(AuthState.Unlocked)
        }
    }

    override suspend fun logout() {
        withContext(ioDispatcher) {
            encryptedStorage[KEY_PIN_CODE] = null
            keystore.deleteEntry(PRIVATE_KEY_ALIAS)
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

    override fun parseCertificate(pem: String): X509Certificate? =
        kotlin.runCatching {
            StringReader(pem).use { reader ->
                (PEMParser(reader).readObject() as? X509CertificateHolder)?.let {
                    JcaX509CertificateConverter().getCertificate(it)
                }
            }
        }.getOrNull()

    companion object {
        const val KEY_PIN_CODE = "user_pin_code"
        const val PRIVATE_KEY_ALIAS = "fi.dvv.digiid.UserProfile"
    }
}