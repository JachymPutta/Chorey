package com.chorey

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.data.HomeModel
import com.chorey.adapter.HomeRecyclerAdapter
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.dialog.CreateChoreDialog
import com.chorey.util.ChoreUtil.makeRandomChore
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


/**
 * Fragment handling individual homes, it diesplays the chores and members associated with the
 * particular home.
 */
class HomeFragment : Fragment(),
    EventListener<DocumentSnapshot>,
    HomeRecyclerAdapter.OnChoreSelectedListener {
    private val args: HomeFragmentArgs by navArgs()
    private lateinit var home : HomeModel

    private lateinit var homeRef: DocumentReference
    private lateinit var hrvAdapter: HomeRecyclerAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestore: FirebaseFirestore

    enum class CurFrag {
        HOME, BOARD, SUMMARY
    }
    private var curFrag = CurFrag.HOME

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

        homeRef = firestore.collection("homes").document(args.homeModel!!.UID)
        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

        val choreQuery: Query = homeRef.collection("chores")

        hrvAdapter = object : HomeRecyclerAdapter(choreQuery, this@HomeFragment) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.allChoresRecycler.visibility = View.GONE
                    binding.noChoresLeftText.visibility = View.VISIBLE
                } else {
                    binding.allChoresRecycler.visibility = View.VISIBLE
                    binding.noChoresLeftText.visibility = View.GONE
                }
            }
        }

        binding.homeName.visibility = View.GONE
        binding.allChoresRecycler.adapter = hrvAdapter
        binding.allChoresRecycler.layoutManager = LinearLayoutManager(view.context)

        // Hooking up buttons
        binding.addChoreButton.setOnClickListener { addChoreHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }

        binding.homeSummaryButton.setOnClickListener { changeUI(CurFrag.SUMMARY) }
        binding.noticeBoardButton.setOnClickListener { changeUI(CurFrag.BOARD) }
        binding.homeToMenuButton.setOnClickListener { changeUI(CurFrag.HOME) }
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.w(TAG, "home:onEvent ", error)
            return
        }

        value?.let {
            val homeModel = value.toObject<HomeModel>()
            if (homeModel != null) {
                onHomeLoaded(homeModel)
            }
        }
    }

    private fun addChoreHandle() {
        // Check if chore limit reached
        val numChores = hrvAdapter.itemCount
        if (numChores >= MAX_CHORES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }

        // Adding a random chore - TESTING
//        homeRef.collection("chores")
//            .add(makeRandomChore(requireContext()))

        // Create a chore dialog
        val action = HomeFragmentDirections.actionHomeFragmentToCreateChoreDialog().apply {
            homeModel = home.copy()
        }
        findNavController().navigate(action)
    }

    private fun addMemberHandle() {

    }

    private fun changeUI(nextFrag: CurFrag) {
       if (curFrag == nextFrag) {
           return
       }
        when (curFrag) {
            CurFrag.HOME -> {
                binding.allChoresRecycler.visibility = View.GONE
                binding.noChoresLeftText.visibility = View.GONE
                binding.addChoreButton.visibility = View.GONE
            }
            CurFrag.SUMMARY -> binding.homeRecyclerTitle.text = ""
            CurFrag.BOARD -> binding.homeRecyclerTitle.text = ""
        }

        when (nextFrag) {
            CurFrag.HOME -> {
                binding.homeRecyclerTitle.setText(R.string.home_all_chores_text)
                binding.allChoresRecycler.visibility = View.VISIBLE
                binding.noChoresLeftText.visibility = View.VISIBLE
                binding.addChoreButton.visibility = View.VISIBLE
            }
            CurFrag.SUMMARY -> binding.homeRecyclerTitle.text = "Summary"
            CurFrag.BOARD -> binding.homeRecyclerTitle.text = "Notice board"
        }

        curFrag = nextFrag
    }

    override fun onStart() {
        super.onStart()
        hrvAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        hrvAdapter.stopListening()
    }

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel
        binding.homeName.text = homeModel.homeName
        binding.homeName.visibility = View.VISIBLE

        //TODO: Bind the rest of the properties
    }

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onChoreSelected(chore: DocumentSnapshot) {
        //TODO Inflate the detail of the particular chore
    }
}