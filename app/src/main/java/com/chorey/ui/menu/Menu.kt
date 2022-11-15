package com.chorey.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.Homes

class Menu : Fragment() {
    private val homes = Homes()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val hrvAdapter = MenuRecyclerViewAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_rooms_recycler)

        setupRecyclerAdapter(hrvAdapter)

        recyclerView.adapter = hrvAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        view.findViewById<Button>(R.id.returnButton).setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_home)
        }
    }

    fun setupRecyclerAdapter(hrvAdapter : MenuRecyclerViewAdapter) {
        hrvAdapter.onItemClick = {
            // TODO: Function behavior
            homeModel ->
                findNavController().navigate(R.id.action_menu_to_home)
        }
        hrvAdapter.homes = homes.list
    }
}