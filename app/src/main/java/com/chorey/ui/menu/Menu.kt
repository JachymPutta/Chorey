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
    private lateinit var mrvAdapter: MenuRecyclerViewAdapter
    private var removeHome = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_rooms_recycler)
        mrvAdapter = MenuRecyclerViewAdapter(viewModel.getHomes()!!)

        setupRecyclerAdapter(mrvAdapter)

        recyclerView.adapter = mrvAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        viewModel.list.observe(viewLifecycleOwner) { newList ->
            Log.d("Menu.kt", " List size is currently ${viewModel.list.value!!.size}")
            mrvAdapter.notifyDataSetChanged()
        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        view.findViewById<Button>(R.id.addHomeButton).setOnClickListener {
            viewModel.addHome(HomeModel())
            mrvAdapter.homes = viewModel.getHomes()!!
        }

        view.findViewById<Button>(R.id.removeHomeButton).setOnClickListener {
            //TODO: UI Change to reflect that we are removing something + confirmation screen
            removeHome = true
        }
    }
    fun setupRecyclerAdapter(mrvAdapter : MenuRecyclerViewAdapter) {
        mrvAdapter.onItemClick = {
            homeModel ->
                if (removeHome && (viewModel.getHomes() != null)) {
                    viewModel.removeHome(homeModel)
                    mrvAdapter.homes = viewModel.getHomes()!!
                    removeHome = false
                } else {
                    val pos = viewModel.list.value?.indexOf(homeModel)
                    val currentId = bundleOf("ID" to pos)
                    Log.d("Menu", "Bundle ${homeModel.homeId} found at pos: $pos . passing --")
                    findNavController().navigate(R.id.action_menu_to_home, currentId)
                }
        }
    }

}