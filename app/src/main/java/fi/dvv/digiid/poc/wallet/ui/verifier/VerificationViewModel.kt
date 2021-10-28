package fi.dvv.digiid.poc.wallet.ui.verifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.Credential
import fi.dvv.digiid.poc.vc.VerifiableCredential
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class VerificationStatus {
    object Loading : VerificationStatus()
    object Invalid : VerificationStatus()
    data class Valid(val credential: Credential) : VerificationStatus()
}

@ExperimentalCoroutinesApi
@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationService: VerificationService
) : ViewModel() {
    private val decodedCredential = MutableSharedFlow<VerifiableCredential?>()

    val credentialScannedEvent = decodedCredential.filterNotNull()

    val credentialValid = credentialScannedEvent.transformLatest {
        emit(VerificationStatus.Loading)
        emit(
            when (verificationService.verify(it)) {
                true -> VerificationStatus.Valid(it.credentialSubject)
                else -> VerificationStatus.Invalid
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, VerificationStatus.Loading)

    val credentialSubject = credentialValid.filterIsInstance<VerificationStatus.Valid>().map {
        it.credential
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    suspend fun processQRCode(contents: String) {
        decodedCredential.emit(verificationService.decodeCredential(ExportedCredential(contents)))
    }
}