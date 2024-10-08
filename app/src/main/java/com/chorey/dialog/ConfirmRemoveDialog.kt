package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chorey.CHORE_COL
import com.chorey.EXPENSE_COL
import com.chorey.HISTORY_COL
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.data.HomeModel
import com.chorey.data.LoggedUserModel.Companion.FIELD_MEMBER_OF
import com.chorey.databinding.DialogConfirmRemoveBinding
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ConfirmRemoveDialog : DialogFragment() {

    var snapshot : DocumentSnapshot? = null
    var name : String? = null
    var isHome : Boolean = false

    private var _binding: DialogConfirmRemoveBinding? = null

    private val viewModel by activityViewModels<UserViewModel>()
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmRemoveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (name == null) dismiss()

        val titleMsg = "Remove $name?"

        binding.confirmRemoveDialogTitle.text = titleMsg
        binding.confirmRemoveButton.setOnClickListener { confirmRemoveHandle() }
        binding.cancelRemoveButton.setOnClickListener { dismiss() }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun confirmRemoveHandle() {
        if (isHome) {
            removeHome()
        } else {
            snapshot!!.reference.delete()
            dismiss()
        }
    }

    private fun removeHome() {
        val user = viewModel.user.value!!
        val home = snapshot!!.toObject<HomeModel>()

        val homeRef = Firebase.firestore.collection(HOME_COL).document(home!!.homeUID)
        val userRef = Firebase.firestore.collection(USER_COL).document(user.UID)

        user.memberOf.remove(home.homeUID)

        userRef.update(FIELD_MEMBER_OF, user.memberOf)

        // Only do this if it's the last member
        deleteCol(homeRef.collection(NOTE_COL))
        deleteCol(homeRef.collection(CHORE_COL))
        deleteCol(homeRef.collection(USER_COL))
        deleteCol(homeRef.collection(EXPENSE_COL))
        deleteCol(homeRef.collection(HISTORY_COL))

        snapshot!!.reference.delete()
        findNavController().navigate(R.id.menuFragment)
        dismiss()
    }

    private fun deleteCol(colRef : CollectionReference) {
        colRef.get().addOnSuccessListener { ds ->
                for (doc in ds.documents) {
                    doc.reference.delete()
                }
            }
            .addOnFailureListener {
                e -> Log.d(TAG, "Error deleting collection ${colRef.id} : $e")
            }
    }

    companion object {
        const val TAG = "ConfirmRemove"
    }
}