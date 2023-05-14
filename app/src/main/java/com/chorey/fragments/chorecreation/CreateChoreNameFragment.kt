package com.chorey.fragments.chorecreation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding = FragmentCreateChoreNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserPickerAdapter(home.users, this, requireContext())
        val columnCount = kotlin.math.min(2, home.users.size)

        binding.userPickerRecycler.layoutManager = GridLayoutManager(requireContext(), columnCount)
        binding.userPickerRecycler.adapter = adapter

        binding.continueButton.setOnClickListener { continueHandle() }
        binding.addMeButton.setOnClickListener { selectMeHandle() }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.addEveryoneButton.setOnClickListener { selectEveryoneHandle() }
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
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

    private fun continueHandle() {
        if (binding.createChoreNameInput.editText!!.text.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Please fill in all the fields.",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            chore.apply {
                choreName = binding.createChoreNameInput.editText?.text.toString()
                choreDescription = binding.createChoreDescription.editText?.text.toString()
                homeId = home.homeUID
                assignedTo = assignees
            }

            choreViewModel.updateChore(chore)
            findNavController().navigate(R.id.createChoreTimeFragment)
        }
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
}