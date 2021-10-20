package fi.dvv.digiid.poc.wallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.R
import fi.dvv.digiid.poc.wallet.ui.auth.AuthActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authState = runBlocking { profileRepository.authState.first() }

        if (authState !is AuthState.Unlocked) {
            val authIntent = Intent(this, AuthActivity::class.java)
            startActivity(authIntent)
            return finish()
        }

        setContentView(R.layout.activity_main)
    }
}