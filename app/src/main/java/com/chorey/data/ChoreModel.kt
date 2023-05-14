package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ChoreModel(
    var choreUID: String = UUID.randomUUID().toString(),
    var choreName: String = "chore_name",
    var choreDescription: String = "",
    var homeId: String = "home_name",
    var isTimed: Boolean = false,
    var whenDue : Long = Long.MAX_VALUE,
    var timeToComplete : Int = 0,
    var assignedTo: ArrayList<String> = arrayListOf(),
    var curAssignee: String = "",
    var points: Int = 0,
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
) : Parcelable
{
    companion object {
        const val FIELD_UID = "choreUID"
        const val FIELD_NAME = "choreName"
        const val FIELD_DESCRIPTION = ""
        const val FIELD_HOME_ID = "homeId"
        const val FIELD_IS_TIMED = "isTimed"
        const val FIELD_WHEN_DUE = "whenDue"
        const val FIELD_TIME_TO_COMPLETE = "timeToComplete"
        const val FIELD_ASSIGNED_TO = "assignedTo"
        const val FIELD_CUR_ASSIGNEE = "curAssignee"
        const val FIELD_POINTS = "points"
        const val FIELD_REPEATS_EVERY = "repeatsEvery"
    }
}
