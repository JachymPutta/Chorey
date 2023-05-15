package com.chorey.fragments.chorecreation

import android.content.res.Resources.Theme
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chorey.Chorey
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.FragmentCreateChoreTimeBinding
import com.chorey.viewmodel.ChoreViewModel
import com.chorey.viewmodel.HomeViewModel

class CreateChoreTimeFragment : Fragment() {

    private lateinit var binding: FragmentCreateChoreTimeBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val choreViewModel by activityViewModels<ChoreViewModel>()

    private lateinit var home : HomeModel
    private lateinit var chore : ChoreModel

    private var curInterval = RepeatInterval.None
    private lateinit var allButtons : ArrayList<Button>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        chore = choreViewModel.chore.value!!
        binding = FragmentCreateChoreTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allButtons = arrayListOf(
            binding.repeatNo, binding.repeatAsNeeded, binding.repeatDaily,
            binding.repeatWeekly, binding.repeatMonthly, binding.repeatAnnually)

        binding.completeButton.setOnClickListener { onCompleteHandle() }
        binding.cancelButton.setOnClickListener { returnToHome() }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        repeatSelectionHandle()
    }

    private fun onCompleteHandle() {
        chore.apply {
            //TODO: update timing data
        }

        choreViewModel.updateChore(chore)
        choreViewModel.addChoreToHome()

        returnToHome()
    }

    private fun repeatSelectionHandle() {
        allButtons.forEach { b -> b.setOnClickListener { highlightButton(b) } }
    }
    private fun highlightButton(button: Button) {

        allButtons.forEach {curButton ->
            if (curButton == button) {
                // Highlight
                curButton.background.setTint(requireContext().getColor(R.color.secondary_color))
            } else {
                // Remove highlight
//                val theme = requireContext().theme
//                val colorPrimaryContainerAttr = com.google.android.material.R.attr.colorPrimaryContainer
//
//                val typedValue = TypedValue()
//                theme.resolveAttribute(colorPrimaryContainerAttr, typedValue, true)
//
//                val colorPrimaryContainer = typedValue.data
//
                curButton.background.setTint(requireContext().getColor(R.color.primary_color))
            }
        }
    }

    private fun returnToHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }

}