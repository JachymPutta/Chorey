package com.chorey.data.model

/**
 * Data class that stores the information about the home
 */
data class Home (
    val homeId: String,
    //TODO: Replace string with the appropriate data classes
    val members: List<String>,
    val chores: List<String>
)