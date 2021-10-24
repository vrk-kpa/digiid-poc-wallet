package fi.dvv.digiid.poc.wallet.di

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import fi.dvv.digiid.poc.data.network.WalletService
import fi.dvv.digiid.poc.data.repositories.CredentialsRepositoryImpl
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

/**
 * Credentials module
 *
 * This module is responsible for creating the [CredentialsRepository] and all the dependencies it
 * requires, including the [WalletService] REST API (the client certificate is injected to the
 * [OkHttpClient] and needs to be recreated, too).
 *
 * As the credentials are bound to the currently selected profile, they need to be cleared away when
 * the user logs out. As the profile selection is implemented as a separate activity, it's possible
 * to scope these bindings to the [ActivityRetainedComponent] which handles the cleaning up part.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialsModule {
    @Binds
    @ActivityRetainedScoped
    fun bindCredentialsRepository(credentialsRepositoryImpl: CredentialsRepositoryImpl): CredentialsRepository

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideWalletService(profileRepository: ProfileRepository): WalletService {
            val certificates = HandshakeCertificates.Builder()
                .addTrustedCertificate(BuildConfig.WALLET_CERTIFICATE_PEM.decodeCertificatePem())
                .heldCertificate(requireNotNull(profileRepository.clientCertificate))
                .build()

            val okHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
                .build()

            val retrofit = Retrofit.Builder().baseUrl(BuildConfig.WALLET_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().findAndRegisterModules()))
                .build()

            return retrofit.create(WalletService::class.java)
        }
    }
}