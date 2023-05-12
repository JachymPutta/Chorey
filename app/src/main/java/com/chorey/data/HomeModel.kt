package com.chorey.data

import android.os.Parcelable
import com.chorey.R
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * Data class that stores the information about the home
 */

@Parcelize
data class HomeModel (
    var homeUID: String = UUID.randomUUID().toString(),
    var homeName: String = "",
    var icon: Int = R.drawable.baseline_home_24,
    val users : ArrayList<String> = arrayListOf()
) : Parcelable
{
    companion object {
        const val FIELD_UID = "homeUID"
        const val FIELD_NAME = "homeName"
        const val FIELD_USERS = "users"
        const val FIELD_ICON = "icon"
    }
}