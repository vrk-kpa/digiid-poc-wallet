package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentPinCodeBinding
import fi.dvv.digiid.poc.wallet.ui.common.inputMethodManager

@AndroidEntryPoint
abstract class BasePINCodeFragment : Fragment() {
    @StringRes
    open val heading: Int = 0

    @StringRes
    open val body: Int = 0

    protected abstract val pinCodeEntryDelegate: PINCodeEntryDelegate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPinCodeBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            delegate = pinCodeEntryDelegate
            pinCodeHeading.text = getString(heading)
            pinCodeBody.text = getString(body)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.pinCodeInput.editText?.let { editText ->
                if (editText.requestFocus()) {
                    context?.inputMethodManager?.showSoftInput(
                        editText,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
            }
        }

        return binding.root
    }
}