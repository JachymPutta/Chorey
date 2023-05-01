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
import com.chorey.databinding.FragmentFundBinding
import com.chorey.databinding.FragmentSummaryBinding
import com.chorey.dialog.HomeDetailDialog
import com.google.firebase.firestore.Query

class FundFragment(
    private val home : HomeModel,
    private val query: Query
) : Fragment() {

    private lateinit var binding: FragmentFundBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    companion object {
        const val TAG = "SummaryFragment"
    }
}