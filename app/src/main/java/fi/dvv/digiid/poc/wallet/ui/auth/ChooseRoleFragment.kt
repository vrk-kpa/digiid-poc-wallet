package fi.dvv.digiid.poc.wallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.wallet.databinding.FragmentChooseRoleBinding

@AndroidEntryPoint
class ChooseRoleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChooseRoleBinding.inflate(inflater, container, false)

        binding.roleButton.setOnClickListener {
            findNavController().navigate(ChooseRoleFragmentDirections.toPINCodeEntry())
        }

        return binding.root
    }
}