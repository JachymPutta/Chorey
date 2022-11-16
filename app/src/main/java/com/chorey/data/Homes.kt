package com.chorey.data

import com.chorey.MAX_HOMES
import com.chorey.data.model.HomeModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Homes {
    private val list : ArrayList<HomeModel> = arrayListOf()
    private val db = Firebase.firestore

    init {
        list.add(HomeModel())
        val testHome = HomeModel()
        testHome.homeId = "Cosy Home"
        testHome.createNew = false
        list.add(testHome)
    }

    fun findHome(uid : String) {
        list.find { home -> home.UID == uid }
    }

    fun getHomes(): ArrayList<HomeModel> {
        return list
    }

    fun addHome(home: HomeModel) {
        if(list.size < MAX_HOMES) {
            // Add locally
            list.add(home)
            // Add to db

            // Ping the interface to update ui
        }
    }

}
