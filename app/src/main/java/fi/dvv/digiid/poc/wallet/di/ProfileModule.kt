package fi.dvv.digiid.poc.wallet.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fi.dvv.digiid.poc.data.repositories.ProfileRepositoryImpl
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ProfileModule {
    @Binds
    @Singleton
    fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository
}