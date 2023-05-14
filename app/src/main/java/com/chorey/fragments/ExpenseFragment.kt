package com.chorey.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chorey.EXPENSE_COL
import com.chorey.HOME_COL
import com.chorey.data.ContribModel
import com.chorey.data.ExpenseModel
import com.chorey.data.ExpenseType
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentExpensesBinding
import com.chorey.dialog.ExpenseDetailDialog
import com.chorey.dialog.ExpenseOtherDialog
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ExpenseFragment : Fragment(),
    ExpenseDetailDialog.OnExpenseChangedListener {

    private val homeViewModel by activityViewModels<HomeViewModel>()

    private lateinit var binding: FragmentExpensesBinding
    private lateinit var firestore: FirebaseFirestore

    private lateinit var home : HomeModel

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
        home = homeViewModel.home.value!!
        binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore

        dataLoaded(false)
        // Get the data from the database
        fetchExpenseData()

        // Update the contributors
        allExpenseModels.forEach { expenseModel ->
            val newContribs = home.users.map { ContribModel(it.name, 0) }
            expenseModel.contributors = newContribs as ArrayList<ContribModel>
        }

        // Button listeners
        setClickListeners()
    }

    override fun onExpenseDataChanged(expense: ExpenseModel) {
        // Update visuals
        updateExpenseModel(expense)
        updateUI(expense)

        // Write to DB
        firestore.collection(HOME_COL).document(home.homeUID)
            .collection(EXPENSE_COL).document(expense.UID).set(expense)

    }

    private fun fetchExpenseData() {
        firestore.collection(HOME_COL).document(home.homeUID)
                .collection(EXPENSE_COL).get()
            .addOnSuccessListener { result ->
            for (doc in result.documents) {
                if (doc.exists()) {
                    val expenseModel = doc.toObject<ExpenseModel>()!!
                    updateExpenseModel(expenseModel)
                }
            }

            allExpenseModels.forEach { model -> updateUI(model)}
            dataLoaded(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "fetchExpenseData: error fetching data: $e")
            }
    }

    private fun updateExpenseModel(expenseModel: ExpenseModel) {
        when(expenseModel.type){
            ExpenseType.Rent -> rentExpenseModel = expenseModel
            ExpenseType.Utilities -> utilExpenseModel = expenseModel
            ExpenseType.Groceries -> groceriesExpenseModel = expenseModel
            ExpenseType.Household -> householdExpenseModel = expenseModel
            ExpenseType.Fun -> funExpenseModel = expenseModel
        }
    }

    private fun updateUI(expenseModel: ExpenseModel) {
        when(expenseModel.type) {
            ExpenseType.Rent -> {
                if (rentExpenseModel.goal == 0) {
                    binding.expenseRentButton.alpha = VAL_ALPHA
                } else {
                    val progressText = "${rentExpenseModel.cur} / ${rentExpenseModel.goal}"
                    binding.rentProgressIndicator.max = rentExpenseModel.goal
                    binding.rentProgressIndicator.progress = rentExpenseModel.cur
                    binding.rentProgressText.text = progressText
                    binding.expenseRentButton.alpha = 1F
                }
            }
            ExpenseType.Utilities -> {
                if (utilExpenseModel.goal == 0) {
                    binding.expenseUtilitiesButton.alpha = VAL_ALPHA
                } else {
                    val progressText = "${utilExpenseModel.cur} / ${utilExpenseModel.goal}"
                    binding.utilitiesProgressIndicator.max = utilExpenseModel.goal
                    binding.utilitiesProgressIndicator.progress = utilExpenseModel.cur
                    binding.utilitiesProgressText.text = progressText
                    binding.expenseUtilitiesButton.alpha = 1F
                }
            }
            ExpenseType.Groceries -> {
                if (groceriesExpenseModel.goal == 0) {
                    binding.expenseGroceriesButton.alpha = VAL_ALPHA
                } else {
                    val progressText = "${groceriesExpenseModel.cur} / ${groceriesExpenseModel.goal}"
                    binding.groceriesProgressIndicator.max = groceriesExpenseModel.goal
                    binding.groceriesProgressIndicator.progress = groceriesExpenseModel.cur
                    binding.groceriesProgressText.text = progressText
                    binding.expenseGroceriesButton.alpha = 1F
                }
            }
            ExpenseType.Household -> {
                if (householdExpenseModel.goal == 0) {
                    binding.expenseHouseholdItemsButton.alpha = VAL_ALPHA
                } else {
                    val progressText = "${householdExpenseModel.cur} / ${householdExpenseModel.goal}"
                    binding.householdItemsProgressIndicator.max = householdExpenseModel.goal
                    binding.householdItemsProgressIndicator.progress = householdExpenseModel.cur
                    binding.householdItemsProgressText.text = progressText
                    binding.expenseHouseholdItemsButton.alpha = 1F
                }
            }
            ExpenseType.Fun -> {
                if (funExpenseModel.goal == 0) {
                    binding.expenseFunButton.alpha = VAL_ALPHA
                } else {
                    val progressText = "${funExpenseModel.cur} / ${funExpenseModel.goal}"
                    binding.funProgressIndicator.max = funExpenseModel.goal
                    binding.funProgressIndicator.progress = funExpenseModel.cur
                    binding.funProgressText.text = progressText
                    binding.expenseFunButton.alpha = 1F
                }
            }
        }
    }

    private fun dataLoaded(isLoaded : Boolean) {
        if (isLoaded) {
            binding.loadingSpinner.visibility = View.GONE
        } else {
            binding.loadingSpinner.visibility = View.VISIBLE
        }

        binding.expenseHouseholdItemsButton.isEnabled = isLoaded
        binding.expenseFunButton.isEnabled = isLoaded
        binding.expenseUtilitiesButton.isEnabled = isLoaded
        binding.expenseRentButton.isEnabled = isLoaded
        binding.expenseOtherButton.isEnabled = isLoaded
        binding.expenseGroceriesButton.isEnabled = isLoaded
    }

    private fun setClickListeners() {
        binding.expenseRentButton.setOnClickListener {
            ExpenseDetailDialog().apply {
                expense = rentExpenseModel
                listener = this@ExpenseFragment
            }.show(childFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseFunButton.setOnClickListener {
            ExpenseDetailDialog().apply {
                expense = funExpenseModel
                listener = this@ExpenseFragment
            }.show(childFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseGroceriesButton.setOnClickListener {
            ExpenseDetailDialog().apply {
                expense = groceriesExpenseModel
                listener = this@ExpenseFragment
            }.show(childFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseHouseholdItemsButton.setOnClickListener {
            ExpenseDetailDialog().apply {
                expense = householdExpenseModel
                listener = this@ExpenseFragment
            }.show(childFragmentManager, ExpenseDetailDialog.TAG)
        }

        binding.expenseUtilitiesButton.setOnClickListener {
            ExpenseDetailDialog().apply {
                expense = utilExpenseModel
                listener = this@ExpenseFragment
            }.show(childFragmentManager, ExpenseDetailDialog.TAG)
        }
        binding.expenseOtherButton.setOnClickListener {
            ExpenseOtherDialog().show(childFragmentManager, ExpenseOtherDialog.TAG)
        }
    }


    companion object {
        const val TAG = "ExpenseFragment"
        const val VAL_ALPHA = 0.7F
    }
}