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
        private val expense: ExpenseModel,
        private val listener: OnExpenseChangedListener
    ) : DialogFragment(),
        ExpenseGoalEditDialog.EditExpenseListener {

    private var _binding: DialogExpenseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var contribAdapter: ContribRecyclerAdapter
    private lateinit var user: LoggedUserModel

    private val userViewModel by activityViewModels<UserViewModel>()

    private var myShare = 0
    private lateinit var myContrib : ContribModel
    private var dataChanged = false

    interface OnExpenseChangedListener {
        fun onExpenseDataChanged(expense: ExpenseModel)
    }

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
        myContrib = expense.contributors.find { user.name == it.contributor }!!
        myShare = expense.goal / (expense.contributors.size * 3)

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

    override fun onStop() {
        super.onStop()

        if (dataChanged) {
            listener.onExpenseDataChanged(expense)
        }
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
        // TODO currency
       if (myContrib.amount == 0) {
            val buttonText = "PAY MY SHARE ($$myShare)"
            binding.addContribButton.text = buttonText
       } else {
            val buttonText = "PAID"
           binding.addContribButton.text = buttonText
           binding.addContribButton.isClickable = false
       }

        binding.expenseName.text = expense.type.name
        binding.expenseProgressTitle.text = progressTitle
        binding.expenseProgressIndicator.max = expense.goal
        binding.expenseProgressIndicator.progress = expense.cur
        binding.expenseProgressText.text = progressText

    }

    private fun editGoalHandle() {
        ExpenseGoalEditDialog(expense,this)
            .show(childFragmentManager, ExpenseGoalEditDialog.TAG)
    }

    private fun addContribHandle() {
        if (expense.cur >= expense.goal) return

        myContrib.amount = myShare
        expense.cur += myShare
        dataChanged = true

        updateUI()
        contribAdapter.notifyDataSetChanged()
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
            dataChanged = true
            expense.goal = goal
            expense.repeatsEvery = repeatInterval
            myShare = expense.goal / expense.contributors.size
            updateUI()
        }
    }
}