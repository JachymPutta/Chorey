package com.chorey.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.navArgs
import com.chorey.CHORE_COL
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.util.OnSwipeTouchListener
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

    private lateinit var choreFragment : ChoreFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var summaryFragment: SummaryFragment

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding
    private lateinit var swipeTouchListener: OnSwipeTouchListener

    private var curFrag = CurFrag.CHORES
    enum class CurFrag {
        CHORES, NOTES, SUMMARY
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: hook up the botton navigation view
        firestore = Firebase.firestore
        homeRef = firestore.collection(HOME_COL).document(args.homeModel.UID)


        choreQuery = homeRef.collection(CHORE_COL)
            .orderBy(ChoreModel.FIELD_WHEN_DUE)

        historyQuery = homeRef.collection(CHORE_COL)
            .orderBy(ChoreModel.FIELD_WHEN_DUE, Query.Direction.DESCENDING)

        noteQuery = homeRef.collection(NOTE_COL)

        summaryQuery  = homeRef.collection(USER_COL)
            .orderBy(HomeUserModel.FIELD_POINTS, Query.Direction.DESCENDING)

        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                    snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                    e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

        supportFragmentManager = requireActivity().supportFragmentManager

        swipeTouchListener = object : OnSwipeTouchListener(requireContext()){
            override fun onSwipeLeft() {
                when(curFrag){
                    CurFrag.CHORES -> {
                        loadFragment(summaryFragment)
                        curFrag = CurFrag.SUMMARY
                    }
                    CurFrag.NOTES -> {
                        loadFragment(noteFragment)
                        curFrag = CurFrag.CHORES
                    }
                    CurFrag.SUMMARY -> {}
                }
            }

            override fun onSwipeRight() {
                when(curFrag){
                    CurFrag.CHORES -> {
                        loadFragment(noteFragment)
                        curFrag = CurFrag.NOTES
                    }
                    CurFrag.NOTES -> {}
                    CurFrag.SUMMARY -> {
                        loadFragment(choreFragment)
                        curFrag = CurFrag.CHORES
                    }
                }
            }
        }

        binding.root.setOnTouchListener(swipeTouchListener)

        binding.homeBottomNav.setOnItemSelectedListener { homeFrag ->
            when (homeFrag.itemId) {
                R.id.homeNavChores -> {
                    loadFragment(choreFragment)
                    curFrag = CurFrag.CHORES
                    choreFragment.requireView().setOnTouchListener(swipeTouchListener)
                    true
                }
                R.id.homeNavNotes -> {
                    loadFragment(noteFragment)
                    curFrag = CurFrag.NOTES
                    true
                }
                R.id.homeNavSummary -> {
                    loadFragment(summaryFragment)
                    curFrag = CurFrag.SUMMARY
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

        choreFragment = ChoreFragment(home, choreQuery, historyQuery)
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