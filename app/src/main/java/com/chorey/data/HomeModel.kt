package com.chorey.data

/**
 * Data class that stores the information about the home
 */
data class HomeModel (
    var homeName: String = "home_name",
    val members: ArrayList<LoggedInUser> = arrayListOf(),
    val chores: ArrayList<ChoreModel> = arrayListOf()
)