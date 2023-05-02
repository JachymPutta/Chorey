package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseCategoryModel(
    var type: ExpenseType = ExpenseType.Other,
) : Parcelable
{
    companion object {
        const val FIELD_TYPE = "type"
    }
}
