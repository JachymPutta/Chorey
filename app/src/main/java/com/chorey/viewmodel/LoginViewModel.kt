package com.chorey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.chorey.util.UserLiveData

class LoginViewModel : ViewModel() {
    enum class AuthState {
        AUTHED, UNAUTHED, INV_AUTH
    }

    val authState = UserLiveData().map {
        user -> if (user != null) {
            AuthState.AUTHED
        } else {
            AuthState.UNAUTHED
        }
    }

}