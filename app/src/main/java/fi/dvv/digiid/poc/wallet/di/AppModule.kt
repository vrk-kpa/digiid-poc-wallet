package fi.dvv.digiid.poc.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fi.dvv.digiid.poc.data.di.DefaultDispatcher
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.data.services.VerificationServiceImpl
import fi.dvv.digiid.poc.data.storage.EncryptedSharedPreferencesStorage
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import fi.dvv.digiid.poc.domain.VerificationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

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
    fun provideVerificationService(@IODispatcher ioDispatcher: CoroutineDispatcher): VerificationService {
        return VerificationServiceImpl(ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideEncryptedStorageManager(@ApplicationContext context: Context): EncryptedStorageManager {
        return EncryptedSharedPreferencesStorage(context)
    }
}