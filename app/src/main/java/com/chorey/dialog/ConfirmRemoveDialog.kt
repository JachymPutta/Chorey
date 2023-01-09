package com.chorey.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chorey.R
import com.google.firebase.firestore.DocumentSnapshot

class ConfirmRemoveDialog : DialogFragment() {
    var homeModel : DocumentSnapshot? = null
    var homeName = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you sure you want to remove $homeName ?")
                .setPositiveButton(R.string.confirm_remove_yes)
                { _, _ ->
                    homeModel!!.reference.delete()
                }
                .setNegativeButton(R.string.confirm_remove_no)
                { _, _ ->
                    homeModel = null
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "ConfirmRemove"
    }
}