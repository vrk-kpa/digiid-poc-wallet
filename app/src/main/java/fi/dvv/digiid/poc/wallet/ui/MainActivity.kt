package fi.dvv.digiid.poc.wallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fi.dvv.digiid.poc.domain.model.AuthState
import fi.dvv.digiid.poc.domain.repository.ProfileRepository
import fi.dvv.digiid.poc.wallet.R
import fi.dvv.digiid.poc.wallet.ui.auth.AuthActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (profileRepository.authState.first() is AuthState.Unlocked) {
                setContentView(R.layout.activity_main)
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileRepository.authState.collect {
                    if (it !is AuthState.Unlocked) {
                        val authIntent = Intent(this@MainActivity, AuthActivity::class.java)
                        startActivity(authIntent)
                        return@collect finish()
                    }
                }
            }
        }
    }
}