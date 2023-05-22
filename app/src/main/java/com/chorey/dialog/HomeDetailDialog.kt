package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.HOME_COL
import com.chorey.HOME_ICON_LIST
import com.chorey.R
import com.chorey.adapter.IconPickerAdapter
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogHomeDetailBinding
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeDetailDialog
    : DialogFragment(),
        IconPickerAdapter.IconPickerDialogListener {
    private var _binding: DialogHomeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var home : HomeModel
    private lateinit var iconDialog: IconPickerDialog


    private val userNames = arrayListOf<String>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    //TODO: might have to add an interface here for the icon updates?

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        _binding = DialogHomeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconDialog = IconPickerDialog().apply{
            iconList = HOME_ICON_LIST
            parentDialog = this@HomeDetailDialog
        }

        home.users.forEach { userNames.add(it.name) }
        binding.homeDetailName.text = home.homeName
        binding.homeDetailMembers.text = userNames.joinToString(", ")

        binding.removeHomeButton.setOnClickListener { removeHomeHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }

        binding.homeDetailPicture.setOnClickListener {
            iconDialog.show(childFragmentManager, IconPickerDialog.TAG)
        }
        binding.homeDetailPicture.setImageResource(home.icon)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onIconSelected(icon: Int) {
        //Update the db
        Firebase.firestore.collection(HOME_COL).document(home.homeUID)
            .update(HomeModel.FIELD_ICON, icon)
            .addOnFailureListener { e -> Log.e(TAG, "onIconSelected: error while updating user icon $e")
            }
        iconDialog.dismiss()

        homeViewModel.home.value!!.icon = icon

        binding.homeDetailPicture.setImageResource(icon)
    }

    private fun addMemberHandle() {
        AddMemberDialog().show(parentFragmentManager, "AddMemberDialog")
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