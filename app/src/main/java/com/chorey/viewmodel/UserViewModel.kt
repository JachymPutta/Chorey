package com.chorey.viewmodel

import androidx.lifecycle.ViewModel
import com.chorey.data.LoggedUserModel

class UserViewModel : ViewModel() {
    var user: LoggedUserModel? = null
}