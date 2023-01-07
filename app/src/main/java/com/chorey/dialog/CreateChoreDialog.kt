package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.data.ChoreModel
import com.chorey.databinding.DialogCreateChoreBinding
import com.chorey.util.ChoreUtil.makeRandomChore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateChoreDialog : DialogFragment() {
    private var _binding: DialogCreateChoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateChoreBinding.inflate(inflater, container, false)

        //TODO bind buttons and stuff

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCreateClicked() {
        val user = Firebase.auth.currentUser

        user?.let {
            val chore = makeRandomChore(requireContext())
            // TODO: pass the home id through the safeargs
            val choresRef = Firebase.firestore.collection("chores")
                .document("").collection("chores")

            choresRef.add(chore)
        }

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "CreateChoreDialog"
    }

}