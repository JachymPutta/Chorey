package com.chorey.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chorey.R
import com.google.firebase.firestore.DocumentSnapshot

class ConfirmRemoveDialog : DialogFragment() {
    var snapshot : DocumentSnapshot? = null
    var name = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you sure you want to remove $name ?")
                .setPositiveButton(R.string.confirm_remove_yes)
                { _, _ ->
                    snapshot!!.reference.delete()
                }
                .setNegativeButton(R.string.confirm_remove_no)
                { _, _ ->
                    snapshot = null
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "ConfirmRemove"
    }
}