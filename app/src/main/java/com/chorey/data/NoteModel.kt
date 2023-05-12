package com.chorey.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class NoteModel(
    var noteUID: String = UUID.randomUUID().toString(),
    var note : String = "",
    var author : String = ""
) : Parcelable
{
    companion object {
        const val FIELD_UID = "noteUID"
        const val FIELD_NOTE = "note"
        const val FIELD_AUTHOR = "author"
    }
}
