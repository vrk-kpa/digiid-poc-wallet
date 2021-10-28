package fi.dvv.digiid.poc.wallet.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fi.dvv.digiid.poc.data.di.DefaultDispatcher
import fi.dvv.digiid.poc.data.di.IODispatcher
import fi.dvv.digiid.poc.data.storage.EncryptedSharedPreferencesStorage
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    @Singleton
    @Binds
    fun bindEncryptedStorageManager(
        encryptedSharedPreferencesStorage: EncryptedSharedPreferencesStorage
    ): EncryptedStorageManager

    companion object {
        @Singleton
        @Provides
        @DefaultDispatcher
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Singleton
        @Provides
        @IODispatcher
        fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}