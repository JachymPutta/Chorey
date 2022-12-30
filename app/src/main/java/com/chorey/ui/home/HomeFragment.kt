package com.chorey.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.HomeViewModel
import com.chorey.data.model.HomeModel

private const val HOME_POS = "ID"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var homePos: Int? = null
    private lateinit var home: HomeModel
    private lateinit var hrvAdapter: HomeRecyclerViewAdapter
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            homePos = it.getInt(HOME_POS)
            //TODO: Not null safe - can get IDs out of range
            home = viewModel.getHomes()!![homePos!!]
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_chores_recycler)
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

    fun setupRecyclerAdapter(hrvAdapter: HomeRecyclerViewAdapter) {
        hrvAdapter.onItemClick = {
            TODO("Inflate the detail of the chore")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param pos - Position of the new home
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance(pos: Int) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(HOME_POS, pos)
                }
            }
    }
}