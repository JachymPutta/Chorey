package com.chorey.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chorey.EXPENSE_COL
import com.chorey.HOME_COL
import com.chorey.data.ContribModel
import com.chorey.data.ExpenseModel
import com.chorey.data.ExpenseType
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentExpensesBinding
import com.chorey.dialog.ExpenseDetailDialog
import com.chorey.dialog.ExpenseOtherDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ExpenseFragment(
    private val home: HomeModel
) : Fragment() {

    private lateinit var binding: FragmentExpensesBinding
    private lateinit var firestore: FirebaseFirestore

    private var rentExpenseModel = ExpenseModel(type = ExpenseType.Rent)
    private var utilExpenseModel = ExpenseModel(type = ExpenseType.Utilities)
    private var groceriesExpenseModel = ExpenseModel(type = ExpenseType.Groceries)
    private var householdExpenseModel = ExpenseModel(type = ExpenseType.Household)
    private var funExpenseModel = ExpenseModel(type = ExpenseType.Fun)

    private val allExpenseModels = arrayListOf(
        rentExpenseModel, utilExpenseModel, funExpenseModel, groceriesExpenseModel, householdExpenseModel
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore

        // Get the data from the database
        fetchExpenseData()

        // Update the contributors
        allExpenseModels.forEach { expenseModel ->
            val newContribs = home.users.map { ContribModel(it, 0) }
            expenseModel.contributors = newContribs as ArrayList<ContribModel>
        }

        // TODO: remove this --- testing only
        rentExpenseModel.goal = 1100
        rentExpenseModel.cur = 200

        // Button listeners
        setClickListeners()
    }

    private fun fetchExpenseData() {
        firestore.collection(HOME_COL).document(home.UID)
                .collection(EXPENSE_COL).get()
            .addOnSuccessListener { result ->
            for (doc in result.documents) {
                if (doc.exists()) {
                    val expenseModel = doc.toObject<ExpenseModel>()!!

                    when(expenseModel.type){
                        ExpenseType.Rent -> rentExpenseModel = expenseModel
                        ExpenseType.Utilities -> utilExpenseModel = expenseModel
                        ExpenseType.Groceries -> groceriesExpenseModel = expenseModel
                        ExpenseType.Household -> householdExpenseModel = expenseModel
                        ExpenseType.Fun -> funExpenseModel = expenseModel
                    }
                }
            }
            updateUI()
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "fetchExpenseData: error fetching data: $e")
        }
    }

    private fun updateUI() {
        if (rentExpenseModel.goal == 0) {
            binding.expenseRentButton.alpha = VAL_ALPHA
        } else {
            val progressText = "${rentExpenseModel.cur} / ${rentExpenseModel.goal}"
            binding.rentProgressIndicator.max = rentExpenseModel.goal
            binding.rentProgressIndicator.progress = rentExpenseModel.cur
            binding.rentProgressText.text = progressText
        }

        if (utilExpenseModel.goal == 0) {
            binding.expenseUtilitiesButton.alpha = VAL_ALPHA
        } else {
            val progressText = "${utilExpenseModel.cur} / ${utilExpenseModel.goal}"
            binding.utilitiesProgressIndicator.max = utilExpenseModel.goal
            binding.utilitiesProgressIndicator.progress = utilExpenseModel.cur
            binding.utilitiesProgressText.text = progressText
        }

        if (groceriesExpenseModel.goal == 0) {
            binding.expenseGroceriesButton.alpha = VAL_ALPHA
        } else {
            val progressText = "${groceriesExpenseModel.cur} / ${groceriesExpenseModel.goal}"
            binding.groceriesProgressIndicator.max = groceriesExpenseModel.goal
            binding.groceriesProgressIndicator.progress = groceriesExpenseModel.cur
            binding.groceriesProgressText.text = progressText
        }

        if (householdExpenseModel.goal == 0) {
            binding.expenseHouseholdItemsButton.alpha = VAL_ALPHA
        } else {
            val progressText = "${householdExpenseModel.cur} / ${householdExpenseModel.goal}"
            binding.householdItemsProgressIndicator.max = householdExpenseModel.goal
            binding.householdItemsProgressIndicator.progress = householdExpenseModel.cur
            binding.householdItemsProgressText.text = progressText
        }

        if (funExpenseModel.goal == 0) {
            binding.expenseFunButton.alpha = VAL_ALPHA
        } else {
            val progressText = "${funExpenseModel.cur} / ${funExpenseModel.goal}"
            binding.funProgressIndicator.max = funExpenseModel.goal
            binding.funProgressIndicator.progress = funExpenseModel.cur
            binding.funProgressText.text = progressText
        }
    }

    private fun setClickListeners() {
        binding.expenseRentButton.setOnClickListener {
            ExpenseDetailDialog(home, rentExpenseModel)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseFunButton.setOnClickListener {
            ExpenseDetailDialog(home, funExpenseModel)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseGroceriesButton.setOnClickListener {
            ExpenseDetailDialog(home, groceriesExpenseModel)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseHouseholdItemsButton.setOnClickListener {
            ExpenseDetailDialog(home, householdExpenseModel)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }

        binding.expenseUtilitiesButton.setOnClickListener {
            ExpenseDetailDialog(home, utilExpenseModel)
                .show(parentFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseOtherButton.setOnClickListener {
            ExpenseOtherDialog(home).show(parentFragmentManager, ExpenseOtherDialog.TAG)
        }
    }


    companion object {
        const val TAG = "ExpenseFragment"
        const val VAL_ALPHA = 0.7F
    }
}