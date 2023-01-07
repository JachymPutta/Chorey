package com.chorey.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chorey.R

/**
 * Dialogue for creating a new home or joining an existing one
 */
class AddHomeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater
//            val view = inflater.inflate(R.layout.dialog_add_home, null)

            builder.setTitle(R.string.add_home_dialog_title)
                .setPositiveButton(R.string.create_home_button) { _, _ ->
                    CreateHomeDialog().show(parentFragmentManager, "CreateNewHome")
                }
                .setNegativeButton(R.string.join_existing_home_button) { _, _ ->
                    JoinHomeDialog().show(parentFragmentManager, "JoinExistingHome")
                }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "AddHomeDialog"
    }

}