package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chorey.data.ExpenseModel
import com.chorey.data.ExpenseType
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentExpensesBinding
import com.chorey.dialog.ExpenseDetailDialog
import com.chorey.dialog.ExpenseOtherDialog

class ExpenseFragment(
    private val home: HomeModel
) : Fragment() {

    private lateinit var binding: FragmentExpensesBinding

    private lateinit var rentExpenseModel: ExpenseModel
    private lateinit var utilExpenseModel: ExpenseModel
    private lateinit var funExpenseModel: ExpenseModel
    private lateinit var groceriesExpenseModel: ExpenseModel
    private lateinit var householdExpenseModel: ExpenseModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button listeners
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.expenseRentButton.setOnClickListener {
            ExpenseDetailDialog(home, rentExpenseModel, ExpenseType.Rent)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseFunButton.setOnClickListener {
            ExpenseDetailDialog(home, funExpenseModel, ExpenseType.Fun)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseGroceriesButton.setOnClickListener {
            ExpenseDetailDialog(home, groceriesExpenseModel, ExpenseType.Groceries)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseHouseholdItemsButton.setOnClickListener {
            ExpenseDetailDialog(home, householdExpenseModel, ExpenseType.Household)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }

        binding.expenseUtilitiesButton.setOnClickListener {
            ExpenseDetailDialog(home, utilExpenseModel, ExpenseType.Utilities)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseOtherButton.setOnClickListener {
            ExpenseOtherDialog(home).show(parentFragmentManager, ExpenseOtherDialog.TAG)
        }
    }


    companion object {
        const val TAG = "ExpenseFragment"
    }
}