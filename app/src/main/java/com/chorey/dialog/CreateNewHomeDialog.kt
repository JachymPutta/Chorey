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
import androidx.fragment.app.viewModels
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogCreateNewHomeBinding
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateNewHomeDialog : DialogFragment() {
    private var createHomeListener: CreateHomeListener? = null
    private var _binding: DialogCreateNewHomeBinding? = null
    private val binding get() = _binding!!

    internal interface CreateHomeListener {
        fun onCreateHome(homeModel: HomeModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateNewHomeBinding.inflate(inflater, container, false)

        binding.createHomeCreateButton.setOnClickListener { onCreateClicked() }
        binding.createHomeCancelButton.setOnClickListener { onCancelClicked() }

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
        val user = Firebase.auth.currentUser
        user?.let {
            val home = HomeModel(binding.createHomeNameInput.editText?.text.toString())
            createHomeListener?.onCreateHome(home)
            Firebase.firestore.collection("homes").add(home)
        }
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "CreateHomeDialog"
    }
}
