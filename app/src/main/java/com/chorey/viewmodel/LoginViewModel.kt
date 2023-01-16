package com.chorey.viewmodel

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.chorey.data.UserModel
import com.chorey.util.UserLiveData

class LoginViewModel : ViewModel() {
    enum class AuthState {
        AUTHED, UNAUTHED, INV_AUTH
    }

    lateinit var launcher: ActivityResultLauncher<Intent>

    var user: UserModel? = null
    val authState = UserLiveData().map {
        user -> if (user != null) {
            AuthState.AUTHED
        } else {
            AuthState.UNAUTHED
        }
    }
}