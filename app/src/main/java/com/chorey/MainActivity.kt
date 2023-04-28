package com.chorey

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chorey.fragments.MenuFragment
import com.chorey.viewmodel.LoginViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.launcher =
            registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
                onSignInResult(result)
            }
        setContentView(R.layout.activity_main)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode != Activity.RESULT_OK) {
            if (response == null) {
                this.finish()
            } else if (response.error != null) {
                Log.d(MenuFragment.TAG, "Error signing in: ${response.error}")
            }
        }
    }
}