package fi.dvv.digiid.poc.wallet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.data.di.DefaultDispatcher
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnlockAppViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel(), PINCodeEntryDelegate {
    override val pinCode = MutableStateFlow("")

    override val error = profileRepository.authState.map {
        if (it is AuthState.Locked && it.failed) R.string.auth_authentication_failed
        else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    override fun submit() {
        viewModelScope.launch(viewModelScope.coroutineContext + defaultDispatcher) {
            profileRepository.unlock(pinCode.value)
        }
    }
}