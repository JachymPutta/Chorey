package com.chorey.ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.HomeViewModel
import com.chorey.data.model.HomeModel

class Menu : Fragment() {
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var hrvAdapter: MenuRecyclerViewAdapter
    private var removeHome = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_rooms_recycler)
        hrvAdapter = MenuRecyclerViewAdapter(viewModel.getHomes()!!)

        setupRecyclerAdapter(hrvAdapter)

        recyclerView.adapter = hrvAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        viewModel.list.observe(viewLifecycleOwner) { newList ->
            Log.d("Menu.kt", " List size is currently ${viewModel.list.value!!.size}")
            hrvAdapter.notifyDataSetChanged()
        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        view.findViewById<Button>(R.id.addHomeButton).setOnClickListener {
            viewModel.addHome(HomeModel())
            hrvAdapter.homes = viewModel.getHomes()!!
        }

        view.findViewById<Button>(R.id.removeHomeButton).setOnClickListener {
            //TODO: UI Change to reflect that we are removing something + confirmation screen
            removeHome = true
        }
    }
    fun setupRecyclerAdapter(hrvAdapter : MenuRecyclerViewAdapter) {
        hrvAdapter.onItemClick = {
                homeModel ->
            // TODO: revert this back to the unique ID
//            val currentId = bundleOf("ID" to homeModel.UID)
            if (removeHome) {
                viewModel.removeHome(homeModel)
                hrvAdapter.homes = viewModel.getHomes()!!
                removeHome = false
            } else {
                val currentId = bundleOf("ID" to homeModel.createNew)
                findNavController().navigate(R.id.action_menu_to_home, currentId)
            }
        }
    }

}