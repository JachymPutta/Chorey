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
import com.chorey.databinding.DialogExpenseDetailBinding
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExpenseDetailDialog(
        private val homeModel: HomeModel,
        private val expense: ExpenseModel,
        private val expenseType: ExpenseType
    ) : DialogFragment() {

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
        user = userViewModel.user.value!!

        myShare = expense.goal / expense.contributors.size

        setupAdapter()

        binding.expenseName.text = expense.name

        // Buttons
        binding.expenseDetailCancelButton.setOnClickListener { this.dismiss() }
        binding.addContribButton.setOnClickListener { addContribHandle() }
    }

    private fun addContribHandle() {

        val newContrib = ContribModel(user.name, myShare)
        expense.contributors.add(newContrib)


    }

    private fun setupAdapter() {
        contribAdapter = ContribRecyclerAdapter(expense.contributors)
        binding.contribRecycler.adapter = contribAdapter
        binding.contribRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        const val TAG = "ExpenseDetailDialog"
    }
}