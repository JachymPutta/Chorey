package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

/**
 * Handles the list of all chores in each of the homes.
 * @param chores : list of all the chores
 */
open class HomeRecyclerViewAdapter(query: Query)
    : FirestoreAdapter<HomeRecyclerViewAdapter.ViewHolder>(query) {

    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(choreModel: ChoreModel?) {

            if (choreModel == null) return

            //TODO: Other parameters
            binding.choreName.text = choreModel.choreName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos).toObject<ChoreModel>())
    }
}