package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.R
import com.chorey.adapter.SummaryRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentSummaryBinding
import com.chorey.dialog.HomeDetailDialog
import com.google.firebase.firestore.Query

class SummaryFragment(
    private val home : HomeModel,
    private val query: Query
) : Fragment()
{
    private lateinit var summaryAdapter: SummaryRecyclerAdapter
    private lateinit var binding: FragmentSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        summaryAdapter = SummaryRecyclerAdapter(query)

        binding.homeName.text = home.homeName
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        binding.homeSettingsButton.setOnClickListener {
            HomeDetailDialog(home).show(parentFragmentManager, "HomeDetailDialog")
        }
        // TODO: add listener to change to history

        binding.homeRecyclerTitle.setText(R.string.home_summary_title_points)
        binding.noChoresLeftText.text = ""

        binding.allChoresRecycler.adapter = summaryAdapter
        binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        summaryAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        summaryAdapter.stopListening()
    }

    companion object {
        const val TAG = "SummaryFragment"
    }
}
