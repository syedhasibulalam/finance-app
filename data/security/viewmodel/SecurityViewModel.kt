// main/java/com/achievemeaalk/freedjf/data/security/viewmodel/SecurityViewModel.kt

package com.achievemeaalk.freedjf.data.security.viewmodel

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.security.biometric.BiometricAuthManager
import com.achievemeaalk.freedjf.data.security.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.annotation.RequiresApi
import android.os.Build

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _isPasscodeEnabled = MutableStateFlow(securityRepository.isPasscodeEnabled())
    val isPasscodeEnabled: StateFlow<Boolean> = _isPasscodeEnabled

    private val _pinValue = MutableStateFlow("")
    val pinValue: StateFlow<String> = _pinValue

    private val _authenticationError = MutableStateFlow<String?>(null)
    val authenticationError: StateFlow<String?> = _authenticationError

    var isLocked = MutableStateFlow(true)

    val biometricAuthState = biometricAuthManager.authenticationState

    val canAuthenticateWithBiometrics = biometricAuthManager.canAuthenticate()

    init {
        biometricAuthManager.authenticationState
            .onEach { state ->
                if (state == BiometricAuthManager.AuthenticationState.Success) {
                    isLocked.value = false
                    biometricAuthManager.resetState()
                }
            }.launchIn(viewModelScope)
    }

    fun onPinValueChange(pin: String) {
        // *** ADDED LOG HERE ***
        Log.d("PinEntryDebug", "PIN value changed: $pin")
        if (pin.length <= 4) {
            _pinValue.value = pin
            if (pin.length == 4) {
                verifyPin()
            }
        }
    }

    fun clearError() {
        _authenticationError.value = null
    }

    private fun verifyPin() {
        // *** ADDED LOG HERE ***
        Log.d("PinEntryDebug", "Verifying PIN: ${_pinValue.value}")
        viewModelScope.launch {
            if (securityRepository.verifyPin(_pinValue.value)) {
                Log.d("PinEntryDebug", "PIN correct. Setting isLocked to false.")
                isLocked.value = false
            } else {
                Log.d("PinEntryDebug", "PIN incorrect.")
                _authenticationError.value = "Invalid PIN"
            }
            _pinValue.value = ""
        }
    }

    fun onPinCleared() {
        _pinValue.value = ""
        _authenticationError.value = null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun authenticateWithBiometrics(activity: FragmentActivity) {
        viewModelScope.launch {
            biometricAuthManager.authenticate(activity)
        }
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setPasscodeEnabled(enabled)
            _isPasscodeEnabled.value = enabled
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            securityRepository.setPin(pin)
        }
    }
    fun refreshPasscodeState() {
        _isPasscodeEnabled.value = securityRepository.isPasscodeEnabled()
    }
}

