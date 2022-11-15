package com.chorey.data.model

import java.util.UUID

/**
 * Data class that stores the information about the home
 */
data class HomeModel (
    val UID: String = UUID.randomUUID().toString(),
    var createNew: Boolean = true,
    var homeId: String = "Create new home!",
    //TODO: Replace string with the appropriate data classes
    val members: List<String> = emptyList(),
    val chores: List<String> = emptyList()
)