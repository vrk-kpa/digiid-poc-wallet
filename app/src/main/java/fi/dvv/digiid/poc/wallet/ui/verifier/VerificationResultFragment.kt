package fi.dvv.digiid.poc.wallet.ui.verifier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentVerificationResultBinding

@AndroidEntryPoint
class VerificationResultFragment : Fragment() {
    private val viewModel: VerificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVerificationResultBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }
}