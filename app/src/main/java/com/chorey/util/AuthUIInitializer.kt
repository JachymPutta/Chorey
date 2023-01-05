package com.chorey.util

import android.content.Context
import androidx.startup.Initializer
import com.chorey.BuildConfig
import com.firebase.ui.auth.AuthUI

class AuthUIInitializer : Initializer<AuthUI> {
    // The host '10.0.2.2' is a special IP address to let the
    // Android emulator connect to 'localhost'.
    private val AUTH_EMULATOR_HOST = "10.0.2.2"
    private val AUTH_EMULATOR_PORT = 9099

    override fun create(context: Context): AuthUI {
        val authUI = AuthUI.getInstance()
        // Use emulators only in debug builds
        if (BuildConfig.DEBUG) {
            authUI.useEmulator(AUTH_EMULATOR_HOST, AUTH_EMULATOR_PORT)
        }
        return authUI
    }

    // No dependencies on other libraries
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}