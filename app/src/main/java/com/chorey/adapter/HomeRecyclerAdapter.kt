package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ChoreModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

/**
 * Handles the list of all chores in each of the homes.
 * @param query : DB query to get all the chores for a home
 */
open class HomeRecyclerAdapter(query: Query, private val listener: OnChoreSelectedListener)
    : FirestoreAdapter<HomeRecyclerAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnChoreSelectedListener {
        fun onChoreSelected(chore : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot,
                listener: OnChoreSelectedListener) {

            choreModel = snapshot.toObject<ChoreModel>() ?: return

            binding.choreName.text = choreModel.choreName
            binding.choreDoneButton.setOnClickListener { onDoneClicked(snapshot) }
            binding.root.setOnClickListener { listener.onChoreSelected(snapshot) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }

    private fun onDoneClicked(snapshot: DocumentSnapshot) {
        snapshot.reference.delete()
    }
}