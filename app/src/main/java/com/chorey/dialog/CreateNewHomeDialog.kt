package com.chorey.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.R
import com.chorey.viewmodel.HomeViewModel
import com.chorey.data.HomeModel
import com.google.android.material.textfield.TextInputLayout

class CreateNewHomeDialog : DialogFragment() {
    private val viewModel: HomeViewModel by activityViewModels()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_create_new_home, null)

            builder.setView(view)
                .setPositiveButton(R.string.create_new_home_yes)
                { dialog, id ->
                    val text = view.findViewById<TextInputLayout>(R.id.createHomeNameInput)!!
                        .editText?.text
                    Log.d("CreateHome Dialog", " New home name: $text")

                    val home = HomeModel()
                    home.homeId = text.toString()
                    viewModel.addHome(home)
                }
                .setNegativeButton(R.string.create_new_home_no)
                { _, _ ->
                    dismiss()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}