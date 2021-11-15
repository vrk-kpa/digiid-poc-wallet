package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentCreateProfileBinding
import fi.dvv.digiid.poc.wallet.ui.common.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CreateProfileFragment : Fragment() {
    private val viewModel: ChooseProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentCreateProfileBinding.inflate(inflater, container, false).run {
        lifecycleOwner = viewLifecycleOwner
        viewModel = this@CreateProfileFragment.viewModel
        root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchAndRepeatWithViewLifecycle {
            viewModel.profileSetEvent.collect {
                findNavController().navigate(CreateProfileFragmentDirections.toCreatePIN())
            }
        }
    }
}