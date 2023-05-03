package com.chorey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chorey.data.LoggedUserModel

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<LoggedUserModel>()
    val user: LiveData<LoggedUserModel> = _user

    fun updateUser(newUser : LoggedUserModel) {
        _user.value = newUser
    }

    fun resetUser() {
        _user.value = null
    }
}