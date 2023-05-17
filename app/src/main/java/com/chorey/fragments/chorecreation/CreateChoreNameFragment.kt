package com.chorey.fragments.chorecreation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chorey.R
import com.chorey.adapter.UserPickerAdapter
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.FragmentCreateChoreNameBinding
import com.chorey.viewmodel.ChoreViewModel
import com.chorey.viewmodel.HomeViewModel
import com.chorey.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout

class CreateChoreNameFragment
    : Fragment(),
    UserPickerAdapter.UserPickerListener{

    private lateinit var binding: FragmentCreateChoreNameBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val choreViewModel by activityViewModels<ChoreViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()

    private lateinit var home : HomeModel
    private lateinit var chore : ChoreModel
    private lateinit var adapter : UserPickerAdapter

    private val assignees = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        choreViewModel.resetChore()
        chore = choreViewModel.chore.value!!
        assignees.addAll(home.users.map {model -> model.name })
        binding = FragmentCreateChoreNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserPickerAdapter(home.users, this, requireContext())
        val columnCount = kotlin.math.min(2, home.users.size)

        binding.userPickerRecycler.layoutManager = GridLayoutManager(requireContext(), columnCount)
        binding.userPickerRecycler.adapter = adapter

        hookUpButtons()
    }

    override fun onUserSelected(homeUserModel: HomeUserModel) {
        if (assignees.contains(homeUserModel.name)) {
            homeUserModel.picked = false
            assignees.remove(homeUserModel.name)
        } else {
            homeUserModel.picked = true
            assignees.add(homeUserModel.name)
        }

        adapter.notifyItemChanged(home.users.indexOf(homeUserModel))
    }

    private fun createHandle() {
        updateChore()
        choreViewModel.addChoreToHome()
        returnToHome()
    }

    private fun continueHandle() {
        updateChore()
        findNavController().navigate(R.id.createChoreTimeFragment)
    }

    private fun updateChore() {
        if (!inputOk()) return

        chore.apply {
            choreName = binding.createChoreNameInput.editText?.text.toString()
            choreDescription = binding.createChoreDescription.editText?.text.toString()
            homeId = home.homeUID
            assignedTo = assignees
            curAssignee = assignees.random()
        }
    }

    private fun inputOk() : Boolean {
        if (binding.createChoreNameInput.editText!!.text.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Please name the task",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (assignees.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Assign to at least one person",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }


    private fun returnToHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }

    private fun selectMeHandle() {
        val me = home.users.find { model ->
            model.name == userViewModel.user.value!!.name
        }!!

        onUserSelected(me)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun selectEveryoneHandle() {
        var allSelected = true

        home.users.forEach { user ->
            if (!user.picked) { allSelected = false }
        }

        if (allSelected) {
            home.users.forEach { onUserSelected(it) }
        } else {
            home.users.forEach { user ->
                if (!user.picked) {
                    onUserSelected(user)
                }
            }
        }
    }

    private fun hookUpButtons() {
        binding.continueButton.setOnClickListener { continueHandle() }
        binding.addMeButton.setOnClickListener { selectMeHandle() }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.addEveryoneButton.setOnClickListener { selectEveryoneHandle() }
        binding.cancelButton.setOnClickListener {returnToHome()}
        binding.createButton.setOnClickListener { createHandle() }
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
}