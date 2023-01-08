package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class ChoreModel(
    var choreName: String = "chore_name",
    var homeName: String = "home_name",
    var isTimed: Boolean = false,
    var whenDue : Date? = null,
    var points: Int = 0,
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
    var choreTemplate: ChoreTemplate = ChoreTemplate.Custom
) : Parcelable
