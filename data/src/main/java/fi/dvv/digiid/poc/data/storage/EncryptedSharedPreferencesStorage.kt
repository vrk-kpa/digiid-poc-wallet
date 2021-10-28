package fi.dvv.digiid.poc.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.dvv.digiid.poc.domain.EncryptedStorageManager
import javax.inject.Inject

class EncryptedSharedPreferencesStorage @Inject constructor(
    @ApplicationContext context: Context
) : EncryptedStorageManager {
    // https://security.googleblog.com/2020/02/data-encryption-on-android-with-jetpack.html
    private val sharedPreferences: SharedPreferences = run {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedSharedPreferences.create(
            "profile_secret_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun set(key: String, value: String?) {
        with (sharedPreferences.edit()) {
            value?.let {
                putString(key, it)
            } ?: remove(key)
            apply()
        }
    }
}