package com.chorey.fragments.chorecreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentCreateChoreNameBinding
import com.chorey.viewmodel.ChoreViewModel
import com.chorey.viewmodel.HomeViewModel

class CreateChoreNameFragment : Fragment() {

    private lateinit var binding: FragmentCreateChoreNameBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val choreViewModel by activityViewModels<ChoreViewModel>()

    private lateinit var home : HomeModel
    private lateinit var chore : ChoreModel

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

        binding.continueButton.setOnClickListener { continueHandle() }
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun continueHandle() {
        if (!binding.createChoreNameInput.editText!!.text.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Please fill in all the fields.",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            chore.apply {
                choreName = binding.createChoreNameInput.editText?.text.toString()
                homeId = home.homeUID
                assignedTo = assignees
            }

            choreViewModel.updateChore(chore)
            findNavController().navigate(R.id.createChoreTimeFragment)
        }

    }
}