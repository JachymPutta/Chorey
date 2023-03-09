package com.chorey.data

data class HomeUserModel(
    var name : String = "",
    var points : Long = 0,
    var contrib : Long = 0
)
{
    companion object {
        const val FIELD_POINTS = "points"
    }
}
