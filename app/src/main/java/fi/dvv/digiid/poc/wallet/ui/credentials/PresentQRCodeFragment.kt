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
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.wallet.databinding.FragmentPresentQrcodeBinding
import javax.inject.Inject

@AndroidEntryPoint
class PresentQRCodeFragment : Fragment() {
    @Inject
    lateinit var credentialsRepository: CredentialsRepository

    private val viewModel: WalletViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPresentQrcodeBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.toolbar.setupWithNavController(findNavController())

        return binding.root
    }
}