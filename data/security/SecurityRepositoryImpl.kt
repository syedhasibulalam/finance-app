package com.achievemeaalk.freedjf.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityRepository {

    private val masterKey by lazy(SYNCHRONIZED) {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences: SharedPreferences by lazy(SYNCHRONIZED) {
        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val sharedPreferences: SharedPreferences by lazy(SYNCHRONIZED) {
        context.getSharedPreferences("monefy_prefs", Context.MODE_PRIVATE)
    }

    override fun isPasscodeEnabled(): Boolean {
        return sharedPreferences.getBoolean(SecurityRepository.KEY_PASSCODE_ENABLED, false)
    }

    override fun setPasscodeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(SecurityRepository.KEY_PASSCODE_ENABLED, enabled).apply()
    }

    override fun getPin(): String? {
        return encryptedSharedPreferences.getString(SecurityRepository.KEY_PIN, null)
    }

    override fun setPin(pin: String) {
        encryptedSharedPreferences.edit().putString(SecurityRepository.KEY_PIN, pin).apply()
    }

    override fun verifyPin(pin: String): Boolean {
        return getPin() == pin
    }
}
