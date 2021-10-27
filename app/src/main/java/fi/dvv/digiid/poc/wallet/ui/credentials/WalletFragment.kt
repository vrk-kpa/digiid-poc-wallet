package fi.dvv.digiid.poc.wallet.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.vc.credential.AgeOver18Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver20Credential
import fi.dvv.digiid.poc.wallet.databinding.FragmentWalletBinding

@AndroidEntryPoint
class WalletFragment : Fragment() {
    private val viewModel: WalletViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWalletBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.toolbar.setupWithNavController(findNavController())

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