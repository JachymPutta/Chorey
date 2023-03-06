package com.chorey.data

data class LoggedUserModel(
    var UID : String = "",
    var name : String = "",
    var points : Long = 0,
    var invites : ArrayList<InviteModel> = arrayListOf(),
    val memberOf : MutableMap<String, String> = mutableMapOf()
)
{
    companion object {
        const val FIELD_UID = "UID"
        const val FIELD_NAME = "name"
        const val FIELD_POINTS = "points"
        const val FIELD_INVITES = "invites"
        const val FIELD_MEMBER_OF = "memberOf"
    }
}
