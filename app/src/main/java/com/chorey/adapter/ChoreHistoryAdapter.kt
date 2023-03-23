package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ChoreModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class ChoreHistoryAdapter(query: Query,
                               private val listener: OnHistorySelectedListener,
                               private val user : LoggedUserModel)
    : FirestoreAdapter<ChoreHistoryAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnHistorySelectedListener {
        fun onHistorySelected(chore : DocumentSnapshot)
    }

    //TODO Create its own row XML file
    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(snapshot: DocumentSnapshot,
                     listener: OnHistorySelectedListener) {
                choreModel = snapshot.toObject<ChoreModel>() ?: return

                // Display the history detail
                binding.root.setOnClickListener { listener.onHistorySelected((snapshot)) }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }
}