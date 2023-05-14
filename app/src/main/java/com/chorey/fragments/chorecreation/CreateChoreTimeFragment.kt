package com.chorey.fragments.chorecreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentCreateChoreTimeBinding
import com.chorey.viewmodel.ChoreViewModel
import com.chorey.viewmodel.HomeViewModel

class CreateChoreTimeFragment : Fragment() {

    private lateinit var binding: FragmentCreateChoreTimeBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val choreViewModel by activityViewModels<ChoreViewModel>()

    private lateinit var home : HomeModel
    private lateinit var chore : ChoreModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        choreViewModel.resetChore()
        chore = choreViewModel.chore.value!!
        binding = FragmentCreateChoreTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.completeButton.setOnClickListener { onCompleteHandle() }
        binding.cancelButton.setOnClickListener { returnToHome() }
    }

    private fun onCompleteHandle() {
        chore.apply {
            //TODO: update timing data
        }

        choreViewModel.updateChore(chore)
        choreViewModel.addChoreToHome()

        returnToHome()
    }

    private fun returnToHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }

}