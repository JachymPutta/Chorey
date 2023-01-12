package com.chorey.data

import android.os.Parcelable
import com.chorey.util.ChoreUtil
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class ChoreModel(
    val UID: String = UUID.randomUUID().toString(),
    var choreName: String = "chore_name",
    var homeId: String = "home_name",
    var isTimed: Boolean = false,
    var whenDue : Date? = null,
    var timeToComplete : Int = ChoreUtil.getPoints(1),
    var assignedTo: ArrayList<String> = arrayListOf(),
    var points: Int = 0,
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
    var choreTemplate: ChoreTemplate = ChoreTemplate.Custom
) : Parcelable
