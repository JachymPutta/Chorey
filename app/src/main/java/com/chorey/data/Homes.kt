package com.chorey.data

import com.chorey.data.model.HomeModel

class Homes {
    val list : MutableList<HomeModel> = mutableListOf()

    init {
        list.add(0, HomeModel())
    }
}
