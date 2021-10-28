package fi.dvv.digiid.poc.wallet.ui.verifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.VerificationService
import fi.dvv.digiid.poc.domain.model.ExportedCredential
import fi.dvv.digiid.poc.vc.VerifiableCredential
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationService: VerificationService
) : ViewModel() {
    private val _decodedCredential = MutableStateFlow<VerifiableCredential?>(null)
    val decodedCredential = _decodedCredential.filterNotNull()

    val credentialValid = decodedCredential
        .transformLatest {
            emit(VerificationStatus.Loading)
            emit(
                when (verificationService.verify(it)) {
                    true -> VerificationStatus.Valid(it.credentialSubject)
                    else -> VerificationStatus.Invalid
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, VerificationStatus.Loading)

    val credentialSubject = credentialValid
        .filterIsInstance<VerificationStatus.Valid>()
        .map { it.credential }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun reset() {
        _decodedCredential.value = null
    }

    suspend fun processQRCode(qrCode: String) {
        if (_decodedCredential.value != null) return
        _decodedCredential.value = verificationService.decodeCredential(ExportedCredential(qrCode))
    }
}