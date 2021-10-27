package fi.dvv.digiid.poc.wallet.ui.credentials

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.vc.Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver18Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver20Credential
import fi.dvv.digiid.poc.vc.credential.FamilyNameCredential
import fi.dvv.digiid.poc.vc.credential.GivenNameCredential
import fi.dvv.digiid.poc.wallet.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {
    private val exportedCredentialType = MutableStateFlow<KClass<out Credential>?>(null)

    val exportedCredential = exportedCredentialType.map {
        it?.let {
            credentialsRepository.exportCredential(it)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val credentialType = exportedCredentialType.map {
        when (it) {
            AgeOver18Credential::class, AgeOver20Credential::class -> R.string.credential_type_age
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun credentialValue(context: Context) = exportedCredentialType.map {
        when (it) {
            AgeOver18Credential::class -> context.getString(R.string.credential_value_age, 18)
            AgeOver20Credential::class -> context.getString(R.string.credential_value_age, 20)
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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

    init {
        viewModelScope.launch {
            kotlin.runCatching {
                credentialsRepository.loadCoreIdentity()
            }.onFailure {
                Timber.e("Unable to load the core identity: $it")
            }
        }
    }

    fun<T: Credential> exportCredential(credentialType: KClass<T>) {
        exportedCredentialType.value = credentialType
    }
}