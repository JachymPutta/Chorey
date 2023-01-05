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
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeModel
import com.chorey.adapter.HomeRecyclerAdapter
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.dialog.AddChoreDialog
import com.chorey.util.ChoreUtil.makeRandomChore
import com.chorey.util.FirestoreInitializer
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
 * TODO: Description
 */
class HomeFragment : Fragment(),
    EventListener<DocumentSnapshot> {
    private val args: HomeFragmentArgs by navArgs()
    private val viewModel by viewModels<LoginViewModel>()
    private var addChoreDialog: AddChoreDialog? = null

    private lateinit var homeRef: DocumentReference
    private lateinit var hrvAdapter: HomeRecyclerAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestore: FirebaseFirestore



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: handle this better
        if (args.homeId == "") {
            return
        }

        firestore = Firebase.firestore

        homeRef = firestore.collection("homes").document(args.homeId)

        // Inflate the layout for this fragment
        val recyclerView = view.findViewById<RecyclerView>(R.id.allChoresRecycler)
        val choreQuery: Query = homeRef.collection("chores")

        hrvAdapter = object : HomeRecyclerAdapter(choreQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.allChoresRecycler.visibility = View.GONE
                } else {
                    binding.allChoresRecycler.visibility = View.VISIBLE
                }
            }
        }

        recyclerView.adapter = hrvAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        addChoreDialog = AddChoreDialog()

        // Hooking up buttons
        binding.addChoreButton.setOnClickListener { addChoreHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }
        binding.homeSummaryButton.setOnClickListener { onSummaryHandle() }
        binding.homeToMenuButton.setOnClickListener {
            // Return to home screen
            findNavController().navigate(R.id.action_homeFragment_to_menuFragment)
        }
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.w(TAG, "home:onEvent ", error)
            return
        }

        value?.let {
            val home = value.toObject<HomeModel>()
            if (home != null) {
                onHomeLoaded(home)
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
        homeRef.collection("chores").add(makeRandomChore(requireContext()))

        // Create a chore dialog
        addChoreDialog?.show(childFragmentManager, AddChoreDialog.TAG)
    }

    private fun addMemberHandle() {
        TODO("Not yet implemented")
    }

    private fun onSummaryHandle() {
        TODO("Not yet implemented")
    }



    override fun onStart() {
        super.onStart()

        if (viewModel.authState.value != LoginViewModel.AuthState.AUTHED) {
            findNavController().navigate(R.id.action_homeFragment_to_menuFragment)
        }

        hrvAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        hrvAdapter.stopListening()
    }

    private fun onHomeLoaded(homeModel: HomeModel) {
        binding.homeName.text = homeModel.homeName
        //TODO: Bind the rest of the properties
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}