package fi.dvv.digiid.poc.wallet.ui.verifier

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.VerifiableCredential
import fi.dvv.digiid.poc.wallet.ui.common.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationService: VerificationService
): ViewModel() {
    private val _scannedCredential = MutableLiveData<VerifiableCredential?>(null)

    val credentialScannedEvent = _scannedCredential.switchMap {
        liveData {
            it?.let { emit(Event(it)) }
        }
    }

    val credentialSubject = _scannedCredential.map {
        it?.credentialSubject
    }

    fun processQRCode(contents: String) {
        viewModelScope.launch {
            _scannedCredential.postValue(verificationService.decodeCredential(ExportedCredential(contents)))
        }
    }
}