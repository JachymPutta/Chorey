package com.chorey.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chorey.R

class ConfirmRemoveDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.confirm_remove_title)
                .setPositiveButton(
                    R.string.confirm_remove_yes,
                    DialogInterface.OnClickListener { dialog, id ->
                        // START THE GAME!
                        //TODO Proceed with removal
                    })
                .setNegativeButton(
                    R.string.confirm_remove_no,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}