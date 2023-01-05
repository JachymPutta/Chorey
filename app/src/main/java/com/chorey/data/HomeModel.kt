package com.chorey.data

/**
 * Data class that stores the information about the home
 */
data class HomeModel (
    var homeName: String = "home_name",
    val chores: ArrayList<ChoreModel> = arrayListOf()
)