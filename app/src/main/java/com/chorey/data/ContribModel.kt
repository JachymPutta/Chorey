package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContribModel(
    var contributor: String = "user_name",
    var amount: Int = 0,
) : Parcelable
{
    companion object
}
