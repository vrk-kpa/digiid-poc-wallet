package fi.dvv.digiid.poc.wallet.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import fi.dvv.digiid.poc.data.services.VerificationServiceImpl
import fi.dvv.digiid.poc.domain.VerificationService

@Module
@InstallIn(ActivityRetainedComponent::class)
interface VerificationModule {
    @ActivityRetainedScoped
    @Binds
    fun bindVerificationService(
        verificationServiceImpl: VerificationServiceImpl
    ): VerificationService
}