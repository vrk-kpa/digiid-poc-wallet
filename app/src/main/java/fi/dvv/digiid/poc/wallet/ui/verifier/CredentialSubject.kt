package fi.dvv.digiid.poc.wallet.ui.verifier

import android.widget.TextView
import androidx.databinding.BindingAdapter
import fi.dvv.digiid.poc.vc.Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver18Credential
import fi.dvv.digiid.poc.vc.credential.AgeOver20Credential
import fi.dvv.digiid.poc.wallet.R

@BindingAdapter("credentialSubject")
fun TextView.bindCredentialSubjectValue(credential: Credential?) {
    if (credential == null) return

    when (credential) {
        is AgeOver18Credential -> renderAgeCredential(18, credential.isAgeOver18)
        is AgeOver20Credential -> renderAgeCredential(20, credential.isAgeOver20)
        else -> text = context.getString(R.string.verify_credential_other)
    }
}

internal fun TextView.renderAgeCredential(age: Int, isOver: Boolean) {
    val label = when (isOver) {
        true -> R.string.verify_credential_age_over
        else -> R.string.verify_credential_age_under
    }

    val icon = when (isOver) {
        true -> R.drawable.ic_success
        else -> R.drawable.ic_failure
    }

    text = context.getString(label, age)
    setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
}