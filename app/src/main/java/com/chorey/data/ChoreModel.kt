package com.chorey.data

import java.util.Date
import java.util.UUID

data class ChoreModel(
    var choreName: String = "chore_name",
    var homeName: String = "home_name",
    val assignedTo: ArrayList<LoggedInUser> = arrayListOf(),
    var isTimed: Boolean = false,
    var whenDue : Date? = null,
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
    var choreTemplate: ChoreTemplate = ChoreTemplate.Custom
)
