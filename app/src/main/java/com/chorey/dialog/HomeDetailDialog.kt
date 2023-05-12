package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.HOME_COL
import com.chorey.R
import com.chorey.adapter.IconPickerAdapter
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogHomeDetailBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeDetailDialog(
    private val home : HomeModel,
    private val state: DialogState = DialogState.EDIT
    )
    : DialogFragment(),
        IconPickerAdapter.IconPickerDialogListener{
    private var _binding: DialogHomeDetailBinding? = null
    private val binding get() = _binding!!

    private val iconList = listOf(
        R.drawable.baseline_home_24,
        R.drawable.baseline_money_24,
        R.drawable.baseline_account_circle_24,
        R.drawable.baseline_note_alt_24
    )

    private lateinit var iconDialog: IconPickerDialog
    private var curIcon = R.drawable.baseline_home_24

    //TODO: might have to add an interface here for the icon updates?

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

        iconDialog = IconPickerDialog().apply{
            iconList = this@HomeDetailDialog.iconList
            parentDialog = this@HomeDetailDialog
        }

        binding.homeDetailName.text = home.homeName
        binding.homeDetailMembers.text = home.users.joinToString(",")

        binding.removeHomeButton.setOnClickListener { removeHomeHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }

        binding.homeDetailPicture.setOnClickListener {
            iconDialog.show(parentFragmentManager, IconPickerDialog.TAG)
        }
        binding.homeDetailPicture.setImageResource(home.icon)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onIconSelected(icon: Int) {
        when (state) {
            DialogState.CREATE -> {
                curIcon = icon
            }
            DialogState.EDIT -> {
                //TODO: do we need to store this anywhere?
//                userViewModel.user.value!!.icon = icon
//                listener.onIconChanged()

                //Update the db
                Firebase.firestore.collection(HOME_COL).document(home.homeUID)
                    .update(HomeModel.FIELD_ICON, icon)
                    .addOnFailureListener { e -> Log.e(TAG, "onIconSelected: error while updating user icon $e")
                    }
                iconDialog.dismiss()
            }
        }

        binding.homeDetailPicture.setImageResource(icon)
    }

    private fun addMemberHandle() {
        AddMemberDialog.newInstance(home)
            .show(parentFragmentManager, "AddMemberDialog")
    }

    private fun removeHomeHandle() {
        Firebase.firestore.collection(HOME_COL).document(home.homeUID).get()
            .addOnSuccessListener { snap ->
                ConfirmRemoveDialog().apply {
                    snapshot = snap
                    name = home.homeName
                    isHome = true
                }.show(parentFragmentManager, "ConfirmRemoveDialog")
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