package com.chorey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chorey.data.HomeModel

class HomeViewModel : ViewModel() {
    private val _home = MutableLiveData<HomeModel>()
    val home: LiveData<HomeModel> = _home

    fun updateHome(newHome : HomeModel) {
        _home.value = newHome
    }
}