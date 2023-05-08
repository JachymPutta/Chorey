package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.adapter.ContribRecyclerAdapter
import com.chorey.data.ContribModel
import com.chorey.data.ExpenseModel
import com.chorey.data.ExpenseType
import com.chorey.data.HomeModel
import com.chorey.data.LoggedUserModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogExpenseDetailBinding
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExpenseDetailDialog(
        private val homeModel: HomeModel,
        private val expense: ExpenseModel
    ) : DialogFragment(),
        ExpenseGoalEditDialog.EditExpenseListener {

    private var _binding: DialogExpenseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var contribAdapter: ContribRecyclerAdapter
    private lateinit var user: LoggedUserModel

    private val userViewModel by activityViewModels<UserViewModel>()

    private var myShare = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExpenseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init values
        user = userViewModel.user.value!!

        myShare = expense.goal / expense.contributors.size

        setupAdapter()

        // UI
        updateUI()

        // Buttons
        binding.expenseDetailCancelButton.setOnClickListener { this.dismiss() }
        binding.addContribButton.setOnClickListener { addContribHandle() }
        binding.expenseMiddleLayout.setOnClickListener { editGoalHandle() }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI() {
        val progressTitle =
            if (expense.repeatsEvery != RepeatInterval.None) {
                "Progress this ${expense.repeatsEvery.name}:"
            } else {
                "Current progress:"
            }
        val progressText = "${expense.cur} / ${expense.goal}"

        binding.expenseName.text = expense.type.name
        binding.expenseProgressTitle.text = progressTitle
        binding.expenseProgressIndicator.max = expense.goal
        binding.expenseProgressIndicator.progress = expense.cur
        binding.expenseProgressText.text = progressText
    }

    private fun editGoalHandle() {
        ExpenseGoalEditDialog(this).show(parentFragmentManager, ExpenseGoalEditDialog.TAG)
    }

    private fun addContribHandle() {
        val contrib = expense.contributors.find { user.name == it.contributor }!!
        contrib.amount = myShare
        //TODO: update the database
    }

    private fun setupAdapter() {
        contribAdapter = ContribRecyclerAdapter(expense.contributors)
        binding.contribRecycler.adapter = contribAdapter
        binding.contribRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        const val TAG = "ExpenseDetailDialog"
    }

    override fun onGoalEdit(goal: Int, repeatInterval: RepeatInterval) {
        if(goal != expense.goal || repeatInterval != expense.repeatsEvery) {
            // Update db and UI
        }
    }
}