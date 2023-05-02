package com.chorey.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chorey.CHORE_COL
import com.chorey.FUND_COL
import com.chorey.HISTORY_COL
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.dialog.HomeDetailDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private lateinit var homeRef: DocumentReference
    private lateinit var home : HomeModel

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var supportFragmentManager : FragmentManager

    private lateinit var choreQuery: Query
    private lateinit var historyQuery: Query
    private lateinit var noteQuery: Query
    private lateinit var summaryQuery: Query
    private lateinit var fundQuery: Query

    private lateinit var choreFragment : ChoreFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var summaryFragment: SummaryFragment
    private lateinit var expenseFragment: ExpenseFragment

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        homeRef = firestore.collection(HOME_COL).document(args.homeModel.UID)

        choreQuery = homeRef.collection(CHORE_COL)
            .orderBy(ChoreModel.FIELD_WHEN_DUE)
        historyQuery = homeRef.collection(HISTORY_COL)
            .orderBy(ChoreModel.FIELD_WHEN_DUE, Query.Direction.DESCENDING)
        noteQuery = homeRef.collection(NOTE_COL)
        summaryQuery  = homeRef.collection(USER_COL)
            .orderBy(HomeUserModel.FIELD_POINTS, Query.Direction.DESCENDING)
        fundQuery = homeRef.collection(FUND_COL)

        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                    snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                    e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

        supportFragmentManager = requireActivity().supportFragmentManager


        binding.homeBottomNav.setOnItemSelectedListener { homeFrag ->
            when (homeFrag.itemId) {
                R.id.homeNavChores -> {
                    loadFragment(choreFragment)
                    true
                }
                R.id.homeNavNotes -> {
                    loadFragment(noteFragment)
                    true
                }
                R.id.homeNavSummary -> {
                    loadFragment(summaryFragment)
                    true
                }
                R.id.homeNavFunds -> {
                    loadFragment(expenseFragment)
                    true
                }
                else -> false
            }
        }

    }

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel

        binding.homeName.text = home.homeName
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        binding.homeSettingsButton.setOnClickListener {
            HomeDetailDialog(home).show(parentFragmentManager, "HomeDetailDialog")
        }

        choreFragment = ChoreFragment(home, choreQuery, historyQuery)
        expenseFragment = ExpenseFragment(home, fundQuery)
        noteFragment = NoteFragment(home, noteQuery)
        summaryFragment = SummaryFragment(home, summaryQuery)

        loadFragment(choreFragment)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeFragContainer, fragment)
        // TODO add animations here
//        transaction.setCustomAnimations()
        transaction.commit()

    }
}