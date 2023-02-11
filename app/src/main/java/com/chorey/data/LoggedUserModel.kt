package com.chorey.data

data class LoggedUserModel(
    var UID : String = "",
    var name : String = "",
    var points : Long = 0,
    var invites : ArrayList<InviteModel> = arrayListOf(),
    val memberOf : MutableMap<String, String> = mutableMapOf()
)
