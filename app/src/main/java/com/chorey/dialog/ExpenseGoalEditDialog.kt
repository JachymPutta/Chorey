package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.chorey.R
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogExpenseGoalEditBinding

class ExpenseGoalEditDialog(private val listener : EditExpenseListener) : DialogFragment() {
    private var _binding: DialogExpenseGoalEditBinding? = null
    private val binding get() = _binding!!

    private var newGoal = 0
    private var newRepeatInterval : RepeatInterval = RepeatInterval.None

    interface EditExpenseListener {
        fun onGoalEdit(goal: Int, repeatInterval: RepeatInterval)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExpenseGoalEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editGoalButton.setOnClickListener { onEditClicked() }
        binding.editGoalCancelButton.setOnClickListener { onCancelClicked() }

        val repeatAdapter =
            ArrayAdapter(requireContext(), R.layout.repetition_spinner_item, RepeatInterval.values())
        repeatAdapter.setDropDownViewResource(R.layout.repetition_spinner_dropdown)
        binding.goalIntervalSpinner.adapter = repeatAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun onEditClicked() {
        listener.onGoalEdit(newGoal, newRepeatInterval)
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "ExpenseEditGoalDialog"
    }
}
