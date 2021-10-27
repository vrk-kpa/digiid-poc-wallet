package fi.dvv.digiid.poc.wallet.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.vc.credential.AgeOver18Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver20Credential
import fi.dvv.digiid.poc.wallet.R
import fi.dvv.digiid.poc.wallet.databinding.FragmentWalletBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WalletFragment : Fragment() {
    private val viewModel: WalletViewModel by activityViewModels()

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWalletBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.toolbar.setupWithNavController(findNavController())

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    lifecycleScope.launch {
                        profileRepository.logout()
                    }
                }
            }
            true
        }

        binding.shareCredentialAge18Button.setOnClickListener {
            viewModel.exportCredential(AgeOver18Credential::class)
            findNavController().navigate(WalletFragmentDirections.toShareCredentials())
        }

        binding.shareCredentialAge20Button.setOnClickListener {
            viewModel.exportCredential(AgeOver20Credential::class)
            findNavController().navigate(WalletFragmentDirections.toShareCredentials())
        }

        binding.verifyButton.setOnClickListener {
            findNavController().navigate(WalletFragmentDirections.toVerificationScanner())
        }

        return binding.root
    }
}