package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.USER_COL
import com.chorey.adapter.SummaryRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.databinding.FragmentSummaryBinding
import com.chorey.util.HomeUtil
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SummaryFragment: Fragment()
{
    private lateinit var summaryAdapter: SummaryRecyclerAdapter
    private lateinit var binding: FragmentSummaryBinding

    private lateinit var query: Query
    private lateinit var home : HomeModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = HomeUtil.getHomeFromArgs(requireArguments())
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeRef = Firebase.firestore.collection(HOME_COL).document(home.homeUID)
        query = homeRef.collection(USER_COL).orderBy(HomeUserModel.FIELD_POINTS, Query.Direction.DESCENDING)

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

        fun newInstance(home : HomeModel): SummaryFragment {
            val fragment = SummaryFragment()
            val args = HomeUtil.addHomeToArgs(Bundle(), home)
            fragment.arguments = args
            return fragment
        }
    }
}
