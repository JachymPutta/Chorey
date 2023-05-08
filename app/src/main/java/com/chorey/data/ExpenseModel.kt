package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ExpenseModel(
    var UID: String = UUID.randomUUID().toString(),
    var goal: Int = 0,
    var type: ExpenseType = ExpenseType.Rent,
    var cur: Int = 0,
    var contributors: ArrayList<ContribModel> = arrayListOf(),
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
) : Parcelable
{

    companion object {
        const val FIELD_CONTRIBUTORS = "contributors"
        const val FIELD_CUR = "cur"
    }
}
