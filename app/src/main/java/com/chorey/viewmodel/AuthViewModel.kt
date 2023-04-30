package com.chorey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val _isAuthed = MutableLiveData<Boolean>()
    val isAuthed: LiveData<Boolean> = _isAuthed

    init {
        _isAuthed.value = FirebaseAuth.getInstance().currentUser != null
    }

    fun onLoginSuccess() {
        _isAuthed.value = true
    }

    fun onLogout() {
        _isAuthed.value = false
        Firebase.auth.signOut()
    }
}
