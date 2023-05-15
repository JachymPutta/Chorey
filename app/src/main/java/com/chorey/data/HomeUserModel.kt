package com.chorey.data

import android.os.Parcelable
import com.chorey.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUserModel(
    var name : String = "",
    var icon : Int = R.drawable.baseline_person_24,
    var picked : Boolean = true,
    var points : Long = 0,
    var contrib : Long = 0
) : Parcelable
{
    companion object {
        const val FIELD_POINTS = "points"
    }
}
