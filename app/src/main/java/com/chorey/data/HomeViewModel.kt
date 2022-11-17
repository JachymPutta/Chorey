package com.chorey.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chorey.MAX_HOMES
import com.chorey.data.model.HomeModel

class HomeViewModel : ViewModel() {
    private val _list = MutableLiveData<List<HomeModel>>()
    val list: LiveData<List<HomeModel>> get() = _list

    init {
        _list.value = listOf()

        // Add dummy elements
        val testHome = HomeModel()
        testHome.homeId = "Cosy Home"
        testHome.createNew = false
        addHome(HomeModel())
        addHome(testHome)
        Log.d("HomeView", " List size is currently ${list.value?.size}")
    }

    fun findHome(uid : String) {
        list.value?.find { home -> home.UID == uid }
    }

    fun getHomes(): List<HomeModel>? {
        return list.value
    }

    fun addHome(home: HomeModel) {
        if(list.value!!.size < MAX_HOMES) {
            // Add locally
            _list.value = _list.value?.plus(home)
            // Add to db
        }
    }

}
