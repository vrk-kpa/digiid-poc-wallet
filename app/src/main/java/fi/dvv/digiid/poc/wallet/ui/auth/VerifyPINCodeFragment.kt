package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import fi.dvv.digiid.poc.wallet.R

class VerifyPINCodeFragment : BasePINCodeFragment() {
    override val heading = R.string.choose_profile_verify_pin_heading
    override val body = R.string.choose_profile_verify_pin_body
    override val pinCodeEntryDelegate: ChooseProfileViewModel by activityViewModels()
    private val args by navArgs<VerifyPINCodeFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinCodeEntryDelegate.verify(args.pinCode)
    }
}