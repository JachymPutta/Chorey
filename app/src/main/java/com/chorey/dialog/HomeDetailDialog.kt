package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.HOME_COL
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogHomeDetailBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeDetailDialog(private val home : HomeModel) : DialogFragment(){
    private var _binding: DialogHomeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHomeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeDetailName.text = home.homeName
        binding.removeHomeButton.setOnClickListener { removeHomeHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun addMemberHandle() {
        AddMemberDialog(home).show(parentFragmentManager, "AddMemberDialog")
    }

    private fun removeHomeHandle() {
        Firebase.firestore.collection(HOME_COL).document(home.UID).get()
            .addOnSuccessListener { snap ->
                ConfirmRemoveDialog(snap, home.homeName, true)
                    .show(parentFragmentManager, "ConfirmRemoveDialog")
                this.dismiss()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching home ${home.homeName} : $e")
            }
    }

    companion object {
        const val TAG = "HomeDetailDialog"
    }
}