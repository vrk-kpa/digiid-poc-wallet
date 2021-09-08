package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.wallet.databinding.FragmentPinCodeBinding
import fi.dvv.digiid.poc.wallet.ui.common.inputMethodManager
import javax.inject.Inject

@AndroidEntryPoint
class PINCodeFragment : Fragment() {
    @Inject
    lateinit var credentialsRepository: CredentialsRepository

    private val viewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPinCodeBinding.inflate(layoutInflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.completed.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(PINCodeFragmentDirections.toMainActivity())
                activity?.finish()
            }
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