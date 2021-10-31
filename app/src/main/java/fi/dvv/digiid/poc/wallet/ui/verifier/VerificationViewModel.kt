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
    // store the decoded credential in a state flow to be able to ignore subsequent scanning
    // results coming from the image analyzer
    private val _decodedCredential = MutableStateFlow<VerifiableCredential?>(null)
    val decodedCredential = _decodedCredential.filterNotNull()

    // due to the public certificate validation, it might take a while to validate the credential
    // provide a loading state in addition to the success / failure states
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

    // publish the validated credential details
    val credentialSubject = credentialValid
        .filterIsInstance<VerificationStatus.Valid>()
        .map { it.credential }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * Reset the current credential
     *
     * As the decoder is run only until a credential is found, whenever a new scanning action is
     * requested by the user (ie. the verifier camera fragment is displayed), the credential
     * [StateFlow] needs to be cleared away.
     */
    fun reset() {
        _decodedCredential.value = null
    }

    /**
     * Attempt to decode the QR code contents, if one hasn't been decoded yet
     *
     * Depending on the speed of the analysis it might be able to publish multiple times before the
     * navigation to the result fragment occurs, creating unnecessary certificate validation.
     */
    suspend fun processQRCode(qrCode: String) {
        if (_decodedCredential.value != null) return
        _decodedCredential.value = verificationService.decodeCredential(ExportedCredential(qrCode))
    }
}