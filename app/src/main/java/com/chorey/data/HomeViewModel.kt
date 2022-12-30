package com.chorey.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chorey.MAX_HOMES
import com.chorey.data.model.HomeModel

class HomeViewModel : ViewModel() {
    private val _list = MutableLiveData<List<HomeModel>>()
    private val list: LiveData<List<HomeModel>> get() = _list

    init {
        _list.value = listOf()

        // Add dummy elements
        val testHome = HomeModel()
        testHome.homeId = "Cosy Home"
        testHome.createNew = false
        addHome(testHome)
        Log.d("HomeView", " List size is currently ${list.value?.size}")

        //TODO: Try to fetch the data from the DB
    }

    fun findHome(uid : String) : HomeModel? {
        return list.value?.find { home -> home.UID == uid }
    }

    fun getHomes(): List<HomeModel>? {
        return list.value
    }

    fun getPos(home: HomeModel) : Int {
        return if (list.value.isNullOrEmpty()) {
            -1
        } else {
            list.value!!.indexOf(home)
        }
    }

    fun addHome(home: HomeModel) {
        if(list.value!!.size < MAX_HOMES) {
            // Add locally
            _list.value = _list.value?.plus(home)
        }
    }

    fun getSize() : Int {
        return if (list.value.isNullOrEmpty()) {
            0
        } else {
            list.value!!.size
        }
    }

    fun removeHome(home: HomeModel) {
        if(list.value!!.isNotEmpty()) {
            _list.value = _list.value!!.minus(home)
            Log.d("HomeView", "Removed ${home.homeId}, new home size ${list.value?.size}")
        }
    }

    //TODO: Add an update DB function which gets called after list modifications
}
