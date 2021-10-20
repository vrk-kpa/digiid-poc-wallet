package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import fi.dvv.digiid.poc.wallet.R
import fi.dvv.digiid.poc.wallet.ui.common.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect

class CreatePINCodeFragment : BasePINCodeFragment() {
    override val heading = R.string.choose_profile_create_pin_heading
    override val body = R.string.choose_profile_create_pin_body
    override val pinCodeEntryDelegate: ChooseProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinCodeEntryDelegate.reset()

        launchAndRepeatWithViewLifecycle {
            pinCodeEntryDelegate.verifyPINCodeEvent.collect {
                findNavController().navigate(CreatePINCodeFragmentDirections.toVerifyPINCode(it))
            }
        }
    }
}