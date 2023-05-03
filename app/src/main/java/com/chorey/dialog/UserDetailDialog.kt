package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.UserIconAdapter
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.DialogUserDetailBinding
import com.chorey.viewmodel.AuthViewModel
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDetailDialog(private val listener : onIconChangedListener) : DialogFragment(),
        UserIconAdapter.UserIconDialogListener
{
    private var _binding: DialogUserDetailBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by activityViewModels<UserViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    private lateinit var iconDialog: UserIconDialog

    interface onIconChangedListener {
        fun onIconChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconDialog = UserIconDialog(this)
        val icon = userViewModel.user.value!!.icon

        binding.userDetailName.text = userViewModel.user.value!!.name
        binding.userIcon.setImageResource(icon)

        binding.userIcon.setOnClickListener {
            iconDialog.show(parentFragmentManager, UserIconDialog.TAG)
        }
        binding.logoutButton.setOnClickListener {
            authViewModel.onLogout()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onIconSelected(icon: Int) {
        binding.userIcon.setImageResource(icon)
        userViewModel.user.value!!.icon = icon
        listener.onIconChanged()

        //Update the db
        Firebase.firestore.collection(USER_COL).document(userViewModel.user.value!!.UID)
            .update(LoggedUserModel.FIELD_ICON, icon)
            .addOnFailureListener { e -> Log.e(TAG, "onIconSelected: error while updating user icon $e")
            }
        iconDialog.dismiss()
    }

    companion object {
        const val TAG = "UserDetailDialog"
    }
}