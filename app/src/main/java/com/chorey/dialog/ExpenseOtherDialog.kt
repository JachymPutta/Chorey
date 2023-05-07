package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.adapter.ExpenseRecyclerAdapter
import com.chorey.data.ExpenseType
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogExpenseOtherBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExpenseOtherDialog(
    private val homeModel: HomeModel
) : DialogFragment(),
    ExpenseRecyclerAdapter.OnExpenseSelectedListener {

    private var _binding: DialogExpenseOtherBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseRecyclerAdapter
    private lateinit var expenseQuery: Query

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        expenseQuery = Firebase.firestore.collection(HOME_COL).document(homeModel.UID)
            .collection("other")

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