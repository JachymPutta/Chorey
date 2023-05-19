package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.USER_COL
import com.chorey.adapter.SummaryRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.FragmentSummaryBinding
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SummaryFragment: Fragment() {
    private lateinit var summaryAdapter: SummaryRecyclerAdapter
    private lateinit var binding: FragmentSummaryBinding

    private lateinit var home : HomeModel

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home.users.sortByDescending { it.points }
        summaryAdapter = SummaryRecyclerAdapter(home.users)

        binding.allChoresRecycler.adapter = summaryAdapter
        binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        const val TAG = "SummaryFragment"
    }
}
