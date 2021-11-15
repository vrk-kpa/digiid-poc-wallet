package fi.dvv.digiid.poc.wallet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.data.di.DefaultDispatcher
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltViewModel
class ChooseProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
): ViewModel(), PINCodeEntryDelegate {
    private var clientCertificate: X509Certificate? = null
    private var selectedPINCode: String? = null

    private val _profileSetEvent = Channel<Unit>(capacity = CONFLATED)
    val profileSetEvent = _profileSetEvent.receiveAsFlow()

    private val _verifyPINCodeEvent = Channel<String>(capacity = CONFLATED)
    val verifyPINCodeEvent = _verifyPINCodeEvent.receiveAsFlow()

    override val pinCode = MutableStateFlow("")
    override val error = MutableStateFlow<Int?>(null)

    val keySecurityLevel = profileRepository.keySecurityLevel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pemText = MutableStateFlow<String?>(null)

    val satu = MutableStateFlow("")

    val certificateValid = pemText.map {
        it?.startsWith("-----BEGIN CERTIFICATE-----\n") == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun createCSR() {
        viewModelScope.launch {
           satu.value.takeIf { it.isNotBlank() }?.let { satu ->
                pemText.value = profileRepository.createSigningRequest(satu)
            }
        }
    }

    fun importCertificate() {
        pemText.value?.let { pem ->
            profileRepository.parseCertificate(pem)?.let {
                clientCertificate = it
                _profileSetEvent.trySend(Unit)
            }
        }
    }

    fun reset() {
        pinCode.value = ""
        selectedPINCode = null
        error.value = null
    }

    fun verify(pinCode: String) {
        selectedPINCode = pinCode
        this.pinCode.value = ""
    }

    override fun submit() {
        if (selectedPINCode == null) {
            if (validatePINCodeFormat(pinCode.value)) _verifyPINCodeEvent.trySend(pinCode.value)
            else pinCode.value = ""
            return
        }

        if (pinCode.value != selectedPINCode) {
            error.value = R.string.choose_profile_verify_pin_error
            return
        }

        viewModelScope.launch(viewModelScope.coroutineContext + defaultDispatcher) {
            profileRepository.setProfile(clientCertificate!!, selectedPINCode!!)
        }
    }

    private fun validatePINCodeFormat(pinCode: String): Boolean {
        return pinCode.matches(Regex("^\\d{6}$"))
    }
}