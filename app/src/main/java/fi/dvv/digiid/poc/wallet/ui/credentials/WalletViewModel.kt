package fi.dvv.digiid.poc.wallet.ui.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.vc.credential.BirthDateCredential
import fi.dvv.digiid.poc.vc.credential.FamilyNameCredential
import fi.dvv.digiid.poc.vc.credential.GivenNameCredential
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    credentialsRepository: CredentialsRepository
) : ViewModel() {
    private val presentation = credentialsRepository.coreIdentity.asLiveData(viewModelScope.coroutineContext)

    val name = presentation.map { presentation ->
        presentation?.let {
            val givenName = presentation.verifiableCredentials.firstNotNullOf {
                it.credentialSubject as? GivenNameCredential
            }.givenName

            val familyName = presentation.verifiableCredentials.firstNotNullOf {
                it.credentialSubject as? FamilyNameCredential
            }.familyName

            "$givenName $familyName"
        }
    }

    val birthDate = presentation.map { presentation ->
        presentation?.verifiableCredentials?.firstNotNullOf {
            it.credentialSubject as? BirthDateCredential
        }?.birthDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    }

    val exportedCredential = presentation.map { presentation ->
        if (presentation == null) return@map null
        credentialsRepository.exportCredential(BirthDateCredential::class)
    }

    init {
        viewModelScope.launch {
            kotlin.runCatching {
                credentialsRepository.loadCoreIdentity()
            }.onFailure {
                Timber.e("Unable to load the core identity")
            }
        }
    }
}