package com.chorey.data

import java.util.Date
import java.util.UUID

data class ChoreModel(
    val UID: String = UUID.randomUUID().toString(),
    var createNew: Boolean = true,
    var homeId: String = "",
    //TODO: Replace string with the appropriate data classes
    val assignedTo: ArrayList<String> = arrayListOf(),
    var isTimed: Boolean = false,
    var whenDue : Date? = null,
    var repeatsEvery: RepeatInterval,
    var choreTemplate: ChoreTemplate
)
