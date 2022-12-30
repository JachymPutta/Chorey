package com.chorey.ui.menu

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.chorey.R

/**
 * Dialogue for creating a new home or joining an existing one
 */
class AddHomeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setPositiveButton(
                    R.string.create_new_home_button,
                    DialogInterface.OnClickListener { dialog, id ->
//                        findNavController().navigate(R.id.action_menuFragment_to_createNewHomeDialog)
                        CreateNewHomeDialog().show(parentFragmentManager, "CreateNewHome")
                    })
                .setNegativeButton(
                    R.string.join_existing_home_button,
                    DialogInterface.OnClickListener { dialog, id ->
                        findNavController().navigate(R.id.action_menuFragment_to_joinHomeDialog)
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


}