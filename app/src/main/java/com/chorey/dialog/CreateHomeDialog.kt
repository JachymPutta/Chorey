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
import com.chorey.HOME_COL
import com.chorey.HOME_ICON_LIST
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.IconPickerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.DialogCreateHomeBinding
import com.chorey.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class CreateHomeDialog : DialogFragment(),
    IconPickerAdapter.IconPickerDialogListener {
    private var _binding: DialogCreateHomeBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by activityViewModels<UserViewModel>()

    private var curIcon = HOME_ICON_LIST.random()

    var listener : CreateHomeListener? = null
    private lateinit var iconDialog: IconPickerDialog

    interface CreateHomeListener {
        fun onHomeCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (listener == null) dismiss()

        iconDialog = IconPickerDialog().apply{
            iconList = HOME_ICON_LIST
            parentDialog = this@CreateHomeDialog
        }

        binding.createHomeCreateButton.setOnClickListener { onCreateClicked() }
        binding.createHomeCancelButton.setOnClickListener { onCancelClicked() }

        binding.homeDetailPicture.setOnClickListener {
            iconDialog.show(parentFragmentManager, IconPickerDialog.TAG)
        }
        // Pressing enter just submits the form
        binding.createHomeNameInput.editText!!.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                onCreateClicked()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    override fun onIconSelected(icon: Int) {
        //Update the db
        curIcon = icon
        iconDialog.dismiss()
        binding.homeDetailPicture.setImageResource(icon)
    }

    private fun onCreateClicked() {
        val user = userViewModel.user.value!!
        val db = Firebase.firestore

        if (binding.createHomeNameInput.editText?.text.isNullOrBlank()) {
            Toast.makeText(activity, "Please Enter Name", Toast.LENGTH_SHORT).show()
            return
        }

        val homeUserModel = HomeUserModel(
            name = user.name,
            icon = user.icon,
        )

        val home = HomeModel(
            homeUID = UUID.randomUUID().toString(),
            homeName = binding.createHomeNameInput.editText?.text.toString(),
            icon = curIcon,
            users = arrayListOf(homeUserModel)
        )
        val homeRef = db.collection(HOME_COL).document(home.homeUID)
        val userRef = db.collection(USER_COL).document(user.UID)

        user.memberOf[home.homeUID] = home.homeName

        db.runBatch {
            it.set(homeRef, home)
            it.update(userRef, LoggedUserModel.FIELD_MEMBER_OF, user.memberOf)
        }

        listener?.onHomeCreated()
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "CreateHomeDialog"
    }
}
