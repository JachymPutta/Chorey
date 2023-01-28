package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class NoteModel(
    var UID: String = UUID.randomUUID().toString(),
    var note : String = "",
    var author : String = ""
) : Parcelable
