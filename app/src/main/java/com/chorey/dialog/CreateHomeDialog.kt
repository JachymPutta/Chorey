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
import com.chorey.HOME_COL
import com.chorey.USER_COL
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.DialogCreateHomeBinding
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class CreateHomeDialog : DialogFragment() {
    private var _binding: DialogCreateHomeBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateHomeBinding.inflate(inflater, container, false)

        binding.createHomeCreateButton.setOnClickListener { onCreateClicked() }
        binding.createHomeCancelButton.setOnClickListener { onCancelClicked() }

        // Pressing enter just submits the form
        binding.createHomeNameInput.editText!!.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                onCreateClicked()
                true
            } else {
                false
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCreateClicked() {
        val user = loginViewModel.user
        val db = Firebase.firestore

        if (binding.createHomeNameInput.editText?.text.isNullOrBlank()) {
            Toast.makeText(activity, "Please Enter Name", Toast.LENGTH_SHORT).show()
            return
        }

        val home = HomeModel(
            UID = UUID.randomUUID().toString(),
            homeName = binding.createHomeNameInput.editText?.text.toString(),
            users = arrayListOf(user!!.name)
        )
        val homeUserModel = HomeUserModel(name = user.name)
        val homeRef = db.collection(HOME_COL).document(home.UID)
        val userRef = db.collection(USER_COL).document(user.UID)

        user.memberOf[home.UID] = home.homeName

        db.runBatch {
            it.set(homeRef, home)
            it.set(homeRef.collection(USER_COL).document(user.name), homeUserModel)
            it.update(userRef, "memberOf", user.memberOf)
        }

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "CreateHomeDialog"
    }
}
