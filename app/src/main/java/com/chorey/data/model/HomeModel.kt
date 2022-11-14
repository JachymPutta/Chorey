package com.chorey.data.model

import com.chorey.R

/**
 * Data class that stores the information about the home
 */
data class HomeModel (
    val homeId: String,
    //TODO: Replace string with the appropriate data classes
    val members: List<String>,
    val chores: List<String>
) {
    constructor() : this("Create new Home!", listOf(), listOf())
}