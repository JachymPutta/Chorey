package com.chorey.data

data class UserModel(
    var UID : String = "",
    var name : String = "",
    var points : Long = 0,
    var invites : ArrayList<InviteModel> = arrayListOf()
)
