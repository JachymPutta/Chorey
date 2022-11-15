package com.chorey.data

import com.chorey.data.model.HomeModel

class Homes {
    val list : ArrayList<HomeModel> = arrayListOf()

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

}
