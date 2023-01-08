package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InviteModel(
    val homeUID : String = "",
    val homeName : String = "",
    val fromUser : String = ""
) : Parcelable
