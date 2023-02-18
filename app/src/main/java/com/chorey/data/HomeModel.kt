package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * Data class that stores the information about the home
 */

@Parcelize
data class HomeModel (
    var UID: String = UUID.randomUUID().toString(),
    var homeName: String = "",
    val users : ArrayList<String> = arrayListOf()
) : Parcelable
{
    companion object {
        const val FIELD_UID = "UID"
        const val FIELD_NAME = "homeName"
        const val FIELD_USERS = "users"
    }
}