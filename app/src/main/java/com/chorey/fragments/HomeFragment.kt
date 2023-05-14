package com.chorey.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chorey.HOME_COL
import com.chorey.R
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.dialog.HomeDetailDialog
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private lateinit var homeRef: DocumentReference
    private lateinit var home : HomeModel

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var supportFragmentManager : FragmentManager

    private lateinit var choreFragment : ChoreFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var summaryFragment: SummaryFragment
    private lateinit var expenseFragment: ExpenseFragment

    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeRef = Firebase.firestore.collection(HOME_COL).document(args.homeModel.homeUID)

        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                    snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                    e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

        supportFragmentManager = requireActivity().supportFragmentManager

        // Ads
//        val adRequest = AdRequest.Builder().build()
//        binding.adViewHome.loadAd(adRequest)

    }

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel
        homeViewModel.updateHome(home)


        binding.homeName.text = home.homeName
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        binding.homeSettingsButton.setOnClickListener {
            HomeDetailDialog().show(parentFragmentManager, "HomeDetailDialog")
        }

        initBottomNav()

        choreFragment = ChoreFragment()
        expenseFragment = ExpenseFragment()
        noteFragment = NoteFragment()
        summaryFragment = SummaryFragment()

        binding.loadingSpinner.visibility = View.GONE

        // Start with Chore fragment
        loadFragment(choreFragment)
        binding.homeBottomNav.selectedItemId = R.id.homeNavChores
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeFragContainer, fragment)
        // TODO add animations here
//        transaction.setCustomAnimations()
        transaction.commit()

    }

    private fun initBottomNav() {
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
}