package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogCreateHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateHomeDialog : DialogFragment() {
    private var _binding: DialogCreateHomeBinding? = null
    private val binding get() = _binding!!

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
        val user = Firebase.auth.currentUser
        user?.let {
            val home = HomeModel(binding.createHomeNameInput.editText?.text.toString())
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
