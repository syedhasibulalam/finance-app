package com.achievemeaalk.freedjf.data.security

interface SecurityRepository {
    fun isPasscodeEnabled(): Boolean
    fun setPasscodeEnabled(enabled: Boolean)
    fun getPin(): String?
    fun setPin(pin: String)
    fun verifyPin(pin: String): Boolean

    companion object {
        const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        const val KEY_PIN = "pin"
    }
}
