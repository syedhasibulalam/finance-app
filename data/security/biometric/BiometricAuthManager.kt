package com.achievemeaalk.freedjf.data.security.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.achievemeaalk.freedjf.R

import androidx.annotation.RequiresApi
import android.os.Build

class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Idle)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun authenticate(activity: FragmentActivity) {
        val executor = context.mainExecutor
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _authenticationState.value = AuthenticationState.Success
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                _authenticationState.value = AuthenticationState.Error(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                _authenticationState.value = AuthenticationState.Failure
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_auth_title))
           .setSubtitle(context.getString(R.string.biometric_auth_subtitle))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun resetState() {
        _authenticationState.value = AuthenticationState.Idle
    }

    sealed class AuthenticationState {
        object Idle : AuthenticationState()
        object Success : AuthenticationState()
        data class Error(val message: String) : AuthenticationState()
        object Failure : AuthenticationState()
    }
}
