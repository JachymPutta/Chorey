package com.chorey.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.CHORE_COL
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.data.HomeModel
import com.chorey.data.LoggedUserModel
import com.chorey.data.LoggedUserModel.Companion.FIELD_MEMBER_OF
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ConfirmRemoveDialog(val snapshot : DocumentSnapshot,
                          val name : String,
                          var isHome : Boolean = false) : DialogFragment() {
    private val viewModel by activityViewModels<LoginViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you sure you want to remove $name ?")
                .setPositiveButton(R.string.confirm_remove_yes)
                { _, _ ->
                    if (isHome) {
                        removeHome()
                    } else {
                        snapshot.reference.delete()
                    }
                }
                .setNegativeButton(R.string.confirm_remove_no)
                { _, _ ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun removeHome() {
        val db = Firebase.firestore
        val user = viewModel.user!!
        val home = snapshot.toObject<HomeModel>()

        val homeRef = db.collection(HOME_COL).document(home!!.UID)
        val userRef = db.collection(USER_COL).document(user.UID)

        user.memberOf.remove(home.UID)

        userRef.update(FIELD_MEMBER_OF, user.memberOf)
        deleteCol(homeRef.collection(NOTE_COL))
        deleteCol(homeRef.collection(CHORE_COL))
        deleteCol(homeRef.collection(USER_COL))

        snapshot.reference.delete()
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