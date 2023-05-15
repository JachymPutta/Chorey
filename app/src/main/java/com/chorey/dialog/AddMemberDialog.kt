package com.chorey.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.INVITE_COL
import com.chorey.USER_COL
import com.chorey.data.HomeModel
import com.chorey.data.InviteModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.DialogAddMemberBinding
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.chorey.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class AddMemberDialog : DialogFragment() {

    private var _binding: DialogAddMemberBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by activityViewModels<UserViewModel>()

    private lateinit var home : HomeModel
    private lateinit var firestore: FirebaseFirestore

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        _binding = DialogAddMemberBinding.inflate(inflater, container, false)

        firestore = Firebase.firestore


        binding.addMemberSendButton.setOnClickListener { onSendClicked() }
        binding.addMemberCancelButton.setOnClickListener { dismiss() }
        binding.addMemberNameInput.editText!!.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                onSendClicked()
                true
            } else {
                false
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Check if the touched view is not an input field or a view that should keep the keyboard open
                if (requireActivity().window.currentFocus !is TextInputLayout) {
                    // Hide the keyboard
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSendClicked() {
        // 1. get the current user from the login model
        val sender = userViewModel.user.value!!        // 2. get the destination user from the text input
        val dest = binding.addMemberNameInput.editText?.text.toString().ifBlank {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_LONG).show()
            return
        }

        val invite = InviteModel(
            homeName = home.homeName,
            homeUID = home.homeUID,
            fromUser = sender.name
        )
        // 3. check whether the user is valid
        firestore.collection(USER_COL).whereEqualTo(LoggedUserModel.FIELD_NAME, dest).get()
            .addOnSuccessListener {snap ->
                if (snap.isEmpty) {
                    Toast.makeText(requireActivity(), "User $dest not found!", Toast.LENGTH_LONG).show()
                } else {
                    // There will only ever be one name
                    val destLoggedUserModel = snap.documents[snap.documents.size - 1].toObject<LoggedUserModel>()
                    val invites = destLoggedUserModel!!.invites

                    if (invites.contains(invite)) {
                        Toast.makeText(requireActivity(),
                            "$dest already invited!",
                                Toast.LENGTH_LONG).show()
                    } else if (destLoggedUserModel.memberOf.containsKey(home.homeUID)) {
                        Toast.makeText(requireActivity(),
                            "$dest already member of ${home.homeName}!",
                                Toast.LENGTH_LONG).show()
                        //TODO: Uncomment the next line and remove the rest - enabled to debug invites
                        invites.add(invite)
                        firestore.collection(USER_COL).document(destLoggedUserModel.UID)
                            .collection(INVITE_COL).add(invite)
                    } else {
                    invites.add(invite)
                    firestore.collection(USER_COL).document(destLoggedUserModel.UID)
                        .collection(INVITE_COL).add(invite)
                    }
                    dismiss()
                }
            }
            .addOnFailureListener {
                e -> Log.w(TAG, "onSendClicked: error fetching user $e")
            }
    }

    companion object {
        const val TAG = "AddMemberDialog"
    }
}