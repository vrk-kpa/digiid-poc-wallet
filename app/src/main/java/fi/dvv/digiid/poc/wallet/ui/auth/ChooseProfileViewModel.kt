package fi.dvv.digiid.poc.wallet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.data.di.DefaultDispatcher
import fi.dvv.digiid.poc.domain.model.UserProfile
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
): ViewModel(), OnProfileClickListener, PINCodeEntryDelegate {
    val profileList = profileRepository.availableProfiles

    private var selectedProfile: UserProfile? = null
    private var selectedPINCode: String? = null

    private val _profileSelectedEvent = Channel<Unit>(capacity = CONFLATED)
    val profileSelectedEvent = _profileSelectedEvent.receiveAsFlow()

    private val _verifyPINCodeEvent = Channel<String>(capacity = CONFLATED)
    val verifyPINCodeEvent = _verifyPINCodeEvent.receiveAsFlow()

    override val pinCode = MutableStateFlow("")
    override val error = MutableStateFlow<Int?>(null)

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
            profileRepository.setProfile(selectedProfile!!, selectedPINCode!!)
        }
    }

    private fun validatePINCodeFormat(pinCode: String): Boolean {
        return pinCode.matches(Regex("^\\d{6}$"))
    }

    override fun chooseProfile(profile: UserProfile) {
        selectedProfile = profile
        _profileSelectedEvent.trySend(Unit)
    }
}