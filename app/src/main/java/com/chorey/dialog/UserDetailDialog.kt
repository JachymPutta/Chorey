package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.databinding.DialogUserDetailBinding
import com.chorey.viewmodel.AuthViewModel
import com.chorey.viewmodel.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserDetailDialog : DialogFragment() {
    private var _binding: DialogUserDetailBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by activityViewModels<UserViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

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

        binding.logoutButton.setOnClickListener {
            authViewModel.onLogout()
            dismiss()
        }

        binding.userDetailName.text = userViewModel.user?.name
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}