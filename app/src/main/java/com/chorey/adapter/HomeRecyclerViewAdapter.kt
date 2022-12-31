package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.ChoreModel

/**
 * Handles the list of all chores in each of the homes.
 * @param chores : list of all the chores
 */
class HomeRecyclerViewAdapter(var chores: List<ChoreModel>): RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: ((ChoreModel) -> Unit)? = null

    inner class ViewHolder(view: View, chores: List<ChoreModel>) : RecyclerView.ViewHolder(view) {
        val assigneeView: TextView
        val dueDateView: TextView
        val choreNameView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            choreNameView = view.findViewById(R.id.choreName)
            assigneeView = view.findViewById(R.id.choreAssignee)
            dueDateView = view.findViewById(R.id.choreDueDate)

            view.setOnClickListener {
                onItemClick?.invoke(chores[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recycler_row, parent, false)

        return ViewHolder(view, chores)
    }

    override fun getItemCount(): Int {
        return chores.size
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.choreNameView.text = chores[pos].UID

        //TODO: Initialize the other parameters
    }
}