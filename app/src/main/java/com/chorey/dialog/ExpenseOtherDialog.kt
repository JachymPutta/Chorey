package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.OTHER_COL
import com.chorey.adapter.ExpenseRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogExpenseOtherBinding
import com.chorey.fragments.NoteFragment
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExpenseOtherDialog : DialogFragment(),
    ExpenseRecyclerAdapter.OnExpenseSelectedListener {

    private var _binding: DialogExpenseOtherBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeModel : HomeModel
    private lateinit var expenseAdapter: ExpenseRecyclerAdapter
    private lateinit var expenseQuery: Query

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeModel = homeViewModel.home.value!!
        _binding = DialogExpenseOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()

        binding.expenseTypeName.text = "Other Expenses"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        expenseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        expenseAdapter.stopListening()
    }

    override fun onExpenseSelected(expense: DocumentSnapshot) {
        //TODO: expense detail
    }

    private fun setupAdapter() {
        expenseQuery = Firebase.firestore.collection(HOME_COL).document(homeModel.homeUID)
            .collection(OTHER_COL)

        expenseAdapter = object : ExpenseRecyclerAdapter(expenseQuery,
            this@ExpenseOtherDialog) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    //Display no expenses
                } else {
                }
            }
        }

        binding.expensesRecycler.adapter = expenseAdapter
        binding.expensesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        const val TAG = "ExpenseTypeDialog"
    }
}