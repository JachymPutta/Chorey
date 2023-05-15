package com.chorey.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.chorey.R
import com.chorey.data.ExpenseModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogExpenseGoalEditBinding
import com.google.android.material.textfield.TextInputLayout

class ExpenseGoalEditDialog : DialogFragment() {
    private var _binding: DialogExpenseGoalEditBinding? = null
    private val binding get() = _binding!!

    private var newGoal = 0
    private var newRepeatInterval : RepeatInterval = RepeatInterval.None

    var expense: ExpenseModel = ExpenseModel()
    var listener : EditExpenseListener? = null

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

        if (listener == null) dismiss()

        binding.editGoalInput.setText(expense.goal.toString())

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

    private fun onEditClicked() {
        newGoal = binding.editGoalInput.text.toString().toInt()
        newRepeatInterval = binding.goalIntervalSpinner.selectedItem as RepeatInterval

        if (newGoal != 0) {
            listener?.onGoalEdit(newGoal, newRepeatInterval)
        }
        this.dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "ExpenseEditGoalDialog"
    }
}
