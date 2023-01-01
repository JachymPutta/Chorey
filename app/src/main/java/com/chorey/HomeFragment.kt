package com.chorey

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.viewmodel.HomeViewModel
import com.chorey.data.HomeModel
import com.chorey.adapter.HomeRecyclerViewAdapter
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.util.HomeUtil
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
    private lateinit var homeRef: DocumentReference
    private lateinit var hrvAdapter: HomeRecyclerViewAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestore: FirebaseFirestore
    val args: HomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore

        homeRef = firestore.collection("homes").document(args.homeId)

        // Inflate the layout for this fragment
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_chores_recycler)
        val choreQuery: Query = homeRef.collection("chores")

        // TODO: Adapt the Adapter object to have the on data changed function
        hrvAdapter = object : HomeRecyclerViewAdapter(choreQuery) {
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

    private fun onHomeLoaded(homeModel: HomeModel) {
        binding.homeName.text = homeModel.homeName
        //TODO: Bind the rest of the properties
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}