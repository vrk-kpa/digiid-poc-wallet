package fi.dvv.digiid.poc.wallet.di

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fi.dvv.digiid.poc.data.network.WalletService
import fi.dvv.digiid.poc.wallet.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(BuildConfig.WALLET_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().findAndRegisterModules()))
            .build()
    }

    @Singleton
    @Provides
    fun provideWalletService(retrofit: Retrofit): WalletService {
        return retrofit.create(WalletService::class.java)
    }
}