package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.DialogCreateHomeBinding
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class CreateHomeDialog : DialogFragment() {
    private var _binding: DialogCreateHomeBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateHomeBinding.inflate(inflater, container, false)

        binding.createHomeCreateButton.setOnClickListener { onCreateClicked() }
        binding.createHomeCancelButton.setOnClickListener { onCancelClicked() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCreateClicked() {
        val user = loginViewModel.user
        val db = Firebase.firestore

        if (binding.createHomeNameInput.editText?.text.isNullOrBlank()) {
            Toast.makeText(activity, "Please Enter Name", Toast.LENGTH_SHORT).show()
            return
        }

        val home = HomeModel(
            UID = UUID.randomUUID().toString(),
            homeName = binding.createHomeNameInput.editText?.text.toString(),
            users = arrayListOf(user!!.name)
        )
        val homeUserModel = HomeUserModel(name = user.name)
        val homeRef = db.collection("homes").document(home.UID)

        db.runBatch {
            it.set(homeRef, home)
            it.set(homeRef.collection("users").document(user.name), homeUserModel)
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
