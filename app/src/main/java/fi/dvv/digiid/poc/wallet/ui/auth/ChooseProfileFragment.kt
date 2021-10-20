package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentChooseProfileBinding
import fi.dvv.digiid.poc.wallet.ui.common.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChooseProfileFragment : Fragment() {
    private val viewModel: ChooseProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChooseProfileBinding.inflate(inflater, container, false)

        binding.profileList.adapter = ProfileListAdapter(viewModel.profileList, viewModel)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchAndRepeatWithViewLifecycle {
            viewModel.profileSelectedEvent.collect {
                findNavController().navigate(ChooseProfileFragmentDirections.toPINCodeEntry())
            }
        }
    }
}