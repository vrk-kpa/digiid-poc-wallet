package fi.dvv.digiid.poc.wallet.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
): ViewModel() {
    val pinCode = MutableLiveData("")
    val error = MutableLiveData<Throwable?>()
    val completed = MutableLiveData(false)

    fun logIn() {
        viewModelScope.launch {
            pinCode.value?.let {
                kotlin.runCatching {
                    credentialsRepository.authorize(it)
                    completed.postValue(true)
                }.onFailure {
                    error.postValue(it)
                }
            }
        }
    }
}