package com.chorey.ui.home

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

class Home : Fragment() {
    val homes = Homes()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.all_rooms_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        //TODO: initialize with the actual list
        recyclerView.adapter = HomeRecyclerViewAdapter(homes.list)
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        view.findViewById<Button>(R.id.returnButton).setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_loginFragment)
        }
    }

}