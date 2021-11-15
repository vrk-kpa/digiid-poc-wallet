package fi.dvv.digiid.poc.data.repositories

import android.content.Context
import android.content.pm.PackageManager.FEATURE_STRONGBOX_KEYSTORE
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.model.KeySecurityLevel
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
    @ApplicationContext private val context: Context,
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
            KeyPair(it.certificate.publicKey, it.privateKey)
        } ?: createKeyPair()
    )

    private fun createKeyPair(): KeyPair {
        fun builder(block: (KeyGenParameterSpec.Builder.() -> Unit)? = null): KeyPair {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )

            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                PRIVATE_KEY_ALIAS,
                PURPOSE_SIGN or PURPOSE_VERIFY
            ).run {
                block?.invoke(this)
                setDigests(DIGEST_SHA256, DIGEST_SHA512, DIGEST_NONE)
                build()
            }

            kpg.initialize(parameterSpec)
            return kpg.generateKeyPair()
        }

        return kotlin.runCatching {
            builder {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(
                        context.packageManager.hasSystemFeature(FEATURE_STRONGBOX_KEYSTORE)
                    )
                }
            }
        }.getOrNull() ?: builder()
    }

    override val keySecurityLevel: Flow<KeySecurityLevel> = keyPair.map {
        val keyInfo = KeyFactory.getInstance(
            it.private.algorithm,
            "AndroidKeyStore"
        ).getKeySpec(it.private, android.security.keystore.KeyInfo::class.java)

        keyInfo.securityLevel()
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

    private fun android.security.keystore.KeyInfo.securityLevel(): KeySecurityLevel {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                when (securityLevel) {
                    SECURITY_LEVEL_STRONGBOX -> KeySecurityLevel.STRONGBOX
                    SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeySecurityLevel.TEE
                    SECURITY_LEVEL_UNKNOWN_SECURE -> KeySecurityLevel.LEGACY
                    else -> KeySecurityLevel.NONE
                }
            }

            @Suppress("DEPRECATION")
            isInsideSecureHardware -> KeySecurityLevel.LEGACY

            else -> KeySecurityLevel.NONE
        }
    }

    companion object {
        const val KEY_PIN_CODE = "user_pin_code"
        const val PRIVATE_KEY_ALIAS = "fi.dvv.digiid.UserProfile"
    }
}