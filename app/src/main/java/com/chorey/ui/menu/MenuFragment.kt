package com.chorey.ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
    //TODO Change this to an ENUM for more readability
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
        viewModel.list.observe(viewLifecycleOwner) {
            Log.d("MenuFragment.kt", " List size is currently ${viewModel.list.value!!.size}")
            mrvAdapter.notifyDataSetChanged()
        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Begin the dialogues of creating/joining a home
        view.findViewById<Button>(R.id.addHomeButton).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_addHomeDialogFragment)
        }

        // Removal
        view.findViewById<Button>(R.id.removeHomeButton).setOnClickListener {
            removeHomeToggle(view)
        }
    }
    fun setupRecyclerAdapter(mrvAdapter : MenuRecyclerViewAdapter) {
        mrvAdapter.onItemClick = {
            homeModel ->
                if (removeHome && (viewModel.getHomes() != null)) {
                    removeHomeUntoggle(requireView(), homeModel)
                } else {
                    val pos = viewModel.list.value?.indexOf(homeModel)
                    val currentId = bundleOf("ID" to pos)
                    Log.d("Menu", "Bundle ${homeModel.homeId} found at pos: $pos . passing --")
                    findNavController().navigate(R.id.action_menu_to_home, currentId)
                }
        }
    }

    /**
     * Triggers the visual and logical changes for removing a home from the list
     * @param view: current view
     */
    fun removeHomeToggle(view: View) {
        // Already removing
        if (removeHome) return

        removeHome = true
        val headText:TextView = view.findViewById(R.id.menuTitleText)
        headText.text = getString(R.string.menu_title_remove)
    }

    /**
     * Reverts the changes done by {@link #removeHomeToggle() removeHomeToggle}
     * @param view: current view
     * @param homeModel: home being removed
     */
    fun removeHomeUntoggle(view: View, homeModel: HomeModel) {
        //TODO: Show confirmation before removal
        viewModel.removeHome(homeModel)
        mrvAdapter.homes = viewModel.getHomes()!!

        val headText:TextView = view.findViewById(R.id.menuTitleText)
        headText.text = getString(R.string.menu_title_default)
        removeHome = false
    }
}