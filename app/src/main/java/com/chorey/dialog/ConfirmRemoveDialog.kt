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

class ConfirmRemoveDialog(
    private val snapshot : DocumentSnapshot,
    private val name : String,
    private var isHome : Boolean = false) : DialogFragment() {

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

        val titleMsg = "Are you sure you want to remove $name ?"

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
            snapshot.reference.delete()
        }
    }

    private fun removeHome() {
        val db = Firebase.firestore
        val user = viewModel.user.value!!
        val home = snapshot.toObject<HomeModel>()

        val homeRef = db.collection(HOME_COL).document(home!!.UID)
        val userRef = db.collection(USER_COL).document(user.UID)

        user.memberOf.remove(home.UID)

        userRef.update(FIELD_MEMBER_OF, user.memberOf)
        deleteCol(homeRef.collection(NOTE_COL))
        deleteCol(homeRef.collection(CHORE_COL))
        deleteCol(homeRef.collection(USER_COL))

        snapshot.reference.delete()
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