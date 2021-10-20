package fi.dvv.digiid.poc.wallet.ui.auth

import androidx.fragment.app.viewModels
import fi.dvv.digiid.poc.wallet.R

class UnlockAppFragment : BasePINCodeFragment() {
    override val heading = R.string.auth_pin_code_heading
    override val body = R.string.auth_pin_code_body
    override val pinCodeEntryDelegate: UnlockAppViewModel by viewModels()
}