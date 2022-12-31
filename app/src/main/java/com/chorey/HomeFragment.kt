package com.chorey

import android.os.Bundle
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
import com.chorey.util.HomeUtil


/**
 * TODO: Description
 */
class HomeFragment : Fragment() {
    private lateinit var home: HomeModel
    private lateinit var hrvAdapter: HomeRecyclerViewAdapter
    private val viewModel: HomeViewModel by activityViewModels()
    val args: HomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_chores_recycler)

        //Populating the Home variable
        if (BuildConfig.DEBUG && args.homeId.toInt() == -1 || viewModel.getHomes() == null) {
            home = HomeUtil.makeRandomHome(requireContext())
        } else {
            viewModel.getHomes()!![args.homeId.toInt()]
        }
        hrvAdapter = HomeRecyclerViewAdapter(home.chores)
        setupRecyclerAdapter(hrvAdapter)
        recyclerView.adapter = hrvAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        // Watch for changes in the list
        //TODO: This should be the chore list not the home list
//        viewModel.list.observe(viewLifecycleOwner) {
//            _ -> hrvAdapter.notifyDataSetChanged()
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun setupRecyclerAdapter(hrvAdapter: HomeRecyclerViewAdapter) {
        hrvAdapter.onItemClick = {
            TODO("Inflate the detail of the chore")
        }
    }
}