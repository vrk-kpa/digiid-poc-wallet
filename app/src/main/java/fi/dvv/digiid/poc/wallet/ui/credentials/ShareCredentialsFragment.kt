package fi.dvv.digiid.poc.wallet.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentShareCredentialsBinding

@AndroidEntryPoint
class ShareCredentialsFragment : Fragment() {
    private val viewModel: WalletViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentShareCredentialsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmButton.setOnClickListener {
            findNavController().navigate(ShareCredentialsFragmentDirections.toPresentQRCode())
        }

        return binding.root
    }
}