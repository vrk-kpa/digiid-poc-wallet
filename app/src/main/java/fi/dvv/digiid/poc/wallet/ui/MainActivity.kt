package fi.dvv.digiid.poc.wallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.domain.repository.CredentialsRepository
import fi.dvv.digiid.poc.wallet.R
import fi.dvv.digiid.poc.wallet.ui.auth.AuthActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var credentialsRepository: CredentialsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (credentialsRepository.coreIdentity.value == null) {
            val authIntent = Intent(this, AuthActivity::class.java)
            startActivity(authIntent)
            return finish()
        }

        setContentView(R.layout.activity_main)
    }
}