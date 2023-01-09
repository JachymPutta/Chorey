package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.chorey.databinding.DialogCreateChoreBinding
import com.chorey.util.ChoreUtil.makeRandomChore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateChoreDialog : DialogFragment() {
    private var _binding: DialogCreateChoreBinding? = null
    private val binding get() = _binding!!
    private val args : CreateChoreDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateChoreBinding.inflate(inflater, container, false)

        binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }
        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }

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

            //TODO: think about the database structure
            //TODO: This threw a null exception - args = null
            Firebase.firestore.collection("chores").document(args.homeModel!!.UID)
                .collection("chores").add(chore)
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