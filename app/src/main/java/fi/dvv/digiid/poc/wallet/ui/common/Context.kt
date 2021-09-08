package fi.dvv.digiid.poc.wallet.ui.common

import android.content.Context
import android.view.inputmethod.InputMethodManager

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager