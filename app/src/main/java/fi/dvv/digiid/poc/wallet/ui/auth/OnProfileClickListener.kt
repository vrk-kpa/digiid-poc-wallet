package fi.dvv.digiid.poc.wallet.ui.auth

import fi.dvv.digiid.poc.domain.model.UserProfile

interface OnProfileClickListener {
    fun chooseProfile(profile: UserProfile)
}