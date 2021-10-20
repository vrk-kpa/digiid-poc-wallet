package fi.dvv.digiid.poc.wallet.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.dvv.digiid.poc.domain.model.UserProfile
import fi.dvv.digiid.poc.wallet.databinding.ItemProfileBinding

class ProfileListAdapter (
    private val items: List<UserProfile>,
    private val onProfileClickListener: OnProfileClickListener,
) : RecyclerView.Adapter<ProfileListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = items[position]

        holder.binding.profileButton.apply {
            text = profile.name
            setOnClickListener {
                onProfileClickListener.chooseProfile(profile)
            }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)
}