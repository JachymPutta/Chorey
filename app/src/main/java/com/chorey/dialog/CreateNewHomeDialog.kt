package com.chorey.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogCreateNewHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateNewHomeDialog : DialogFragment() {
    private var createHomeListener: CreateHomeListener? = null
    private var _binding: DialogCreateNewHomeBinding? = null
    private val binding get() = _binding!!

    internal interface CreateHomeListener {
        fun onCreate(homeModel: HomeModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateNewHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (parentFragment is CreateHomeListener) {
           createHomeListener = parentFragment as CreateHomeListener
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCreateClicked() {
        //TODO: Authentication
//        val user = Firebase.auth.currentUser
//        user?.let {
            val home = HomeModel(binding.createHomeNameInput.editText?.text.toString())
            createHomeListener?.onCreate(home)
//        }
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "CreateHomeDialog"
    }
}

// TODO: Remove this code or integrate it back
//    @SuppressLint("InflateParams")
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return activity?.let {
//            // Use the Builder class for convenient dialog construction
//            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater
//            val view = inflater.inflate(R.layout.dialog_create_new_home, null)
//
//            builder.setView(view)
//                .setPositiveButton(R.string.create_new_home_yes) { _, _ -> onCreateClicked() }
//                .setNegativeButton(R.string.create_new_home_no) { _, _ -> onCancelClicked() }
//            // Create the AlertDialog object and return it
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }
