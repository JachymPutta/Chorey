package com.chorey.data

import com.chorey.data.model.HomeModel

class Homes {
    val list : ArrayList<HomeModel> = arrayListOf()

    init {
        list.add(0, HomeModel())
    }
}
