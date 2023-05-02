package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ExpenseModel(
    var UID: String = UUID.randomUUID().toString(),
    var goal: Int = 0,
    var cur: Int = 0,
    var contributors: Map<String, Int> = emptyMap(),
    var repeatsEvery: RepeatInterval = RepeatInterval.None,
) : Parcelable
{
    companion object {
        const val FIELD_CONTRIBUTORS = "contributors"
        const val FIELD_CUR = "cur"
    }
}
