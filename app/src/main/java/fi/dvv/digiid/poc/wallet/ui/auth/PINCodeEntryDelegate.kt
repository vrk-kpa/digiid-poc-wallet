package fi.dvv.digiid.poc.wallet.ui.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface PINCodeEntryDelegate {
    val pinCode: MutableStateFlow<String>
    val error: StateFlow<Int?>

    fun submit()
}