package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.IconPickerAdapter
import com.chorey.data.DialogState
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.DialogUserDetailBinding
import com.chorey.viewmodel.AuthViewModel
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDetailDialog : DialogFragment(),
        IconPickerAdapter.IconPickerDialogListener
{
    var listener : OnIconChangedListener? = null
    var state: DialogState = DialogState.EDIT

    private var _binding: DialogUserDetailBinding? = null
    private val binding get() = _binding!!

    private val userViewModel by activityViewModels<UserViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    private val iconList = listOf(
        R.drawable.baseline_home_24,
        R.drawable.baseline_money_24,
        R.drawable.baseline_account_circle_24,
        R.drawable.baseline_note_alt_24
    )

    private lateinit var iconDialog: IconPickerDialog
    private var curIcon = R.drawable.baseline_person_24

    interface OnIconChangedListener {
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


        iconDialog = IconPickerDialog(iconList, this)

        when(state) {
            DialogState.CREATE -> {
                isCancelable = false
                binding.userIconHint.visibility = View.VISIBLE
                binding.userIconHint.setOnClickListener {
                    iconDialog.show(parentFragmentManager, IconPickerDialog.TAG)
                }

                binding.userDetailName.setOnKeyListener { _, keyCode, event ->
                    if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        submitNameHandle()
                        true
                    } else {
                        false
                    }
                }
                binding.logoutButton.setText(R.string.user_detail_submit)
                binding.logoutButton.setOnClickListener { submitNameHandle() }
            }
            DialogState.EDIT -> {
                curIcon = userViewModel.user.value!!.icon
                binding.userIconHint.visibility = View.GONE

                binding.userDetailName.editText!!.isEnabled = false
                binding.userDetailName.editText!!.isFocusable = false
                binding.userDetailName.editText!!.setText(userViewModel.user.value!!.name)


                binding.logoutButton.setOnClickListener {
                    authViewModel.onLogout()
                    userViewModel.resetUser()
                    dismiss()
                }
            }
        }

        binding.userIcon.setOnClickListener {
            iconDialog.show(parentFragmentManager, IconPickerDialog.TAG)
        }
        binding.userIcon.setImageResource(curIcon)
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
                userViewModel.user.value!!.icon = icon
                listener?.onIconChanged() ?: this.dismiss()

                //Update the db
                Firebase.firestore.collection(USER_COL).document(userViewModel.user.value!!.UID)
                    .update(LoggedUserModel.FIELD_ICON, icon)
                    .addOnFailureListener { e -> Log.e(TAG, "onIconSelected: error while updating user icon $e")
                    }
                iconDialog.dismiss()
            }
        }

        binding.userIcon.setImageResource(icon)
    }

    private fun submitNameHandle() {
        val nameInput = binding.userDetailName.editText!!
        if (nameInput.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Please input a name.", Toast.LENGTH_SHORT).show()
        } else {
            val loggedUserModel = LoggedUserModel(
                UID = Firebase.auth.currentUser!!.uid,
                name = nameInput.text.toString(),
                icon = curIcon
            )
            userViewModel.updateUser(loggedUserModel)
            Firebase.firestore.collection(USER_COL).document(loggedUserModel.UID).set(loggedUserModel)
        }
        dismiss()
    }


    companion object {
        const val TAG = "UserDetailDialog"
    }
}