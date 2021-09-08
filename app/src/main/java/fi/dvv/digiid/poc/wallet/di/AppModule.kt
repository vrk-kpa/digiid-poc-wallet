package fi.dvv.digiid.poc.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fi.dvv.digiid.poc.data.CredentialsRepositoryImpl
import fi.dvv.digiid.poc.data.VerificationServiceImpl
import fi.dvv.digiid.poc.data.WalletService
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class DefaultDispatcher

@Qualifier
annotation class IODispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Singleton
    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideCredentialsRepository(walletService: WalletService): CredentialsRepository {
        return CredentialsRepositoryImpl(walletService)
    }

    @Singleton
    @Provides
    fun provideVerificationService(@IODispatcher ioDispatcher: CoroutineDispatcher): VerificationService {
        return VerificationServiceImpl(ioDispatcher)
    }
}