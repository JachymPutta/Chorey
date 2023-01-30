package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.chorey.data.HomeModel
import com.chorey.data.InviteModel
import com.chorey.data.UserModel
import com.chorey.databinding.DialogAddMemberBinding
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class AddMemberDialog : DialogFragment() {

    private var _binding: DialogAddMemberBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by activityViewModels<LoginViewModel>()
    private val args: AddMemberDialogArgs by navArgs()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var home : HomeModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddMemberBinding.inflate(inflater, container, false)

        home = args.homeModel
        firestore = Firebase.firestore

        binding.addMemberSendButton.setOnClickListener { onSendClicked() }
        binding.addMemberCancelButton.setOnClickListener { dismiss() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSendClicked() {
        // 1. get the current user from the login model
        var success = true
        // TODO: this could throw and error instead of failing silently - but should never happen
        val sender = loginViewModel.user ?: return
        // 2. get the destination user from the text input
        val dest = binding.addMemberNameInput.editText?.text.toString().ifBlank {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_LONG).show()
            return
        }

        val invite = InviteModel(
            homeName = home.homeName,
            homeUID = home.UID,
            fromUser = sender.name
        )
        // 3. check whether the user is valid
        firestore.collection("users").whereEqualTo("name", dest).get()
            .addOnSuccessListener {snap ->
                if (snap.isEmpty) {
                    Toast.makeText(requireContext(), "User $dest not found!", Toast.LENGTH_LONG).show()
                    success = false
                } else {
                    // There will only ever be one name
                    val destUserModel = snap.documents[0].toObject<UserModel>()
                    val invites = destUserModel!!.invites

                    if (invites.contains(invite)) {
                        Toast.makeText(requireContext(), "$dest already invited!", Toast.LENGTH_LONG).show()
                        success = false
                    } else {
                        invites.add(invite)
                        firestore.collection("users").document(destUserModel.UID)
                            .collection("invites").add(invite)
                    }
                }
            }
            .addOnFailureListener {
                e -> Log.w(TAG, "onSendClicked: error fetching user $e")
            }

        if (success) { dismiss() }
    }

    companion object {
        const val TAG = "AddMemberDialog"
    }

}