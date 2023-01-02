package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.databinding.MenuRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

open class JoinHomeRecyclerAdapter(query: Query, private val listener: OnJoinSelectedListener) :
    FirestoreAdapter<JoinHomeRecyclerAdapter.ViewHolder>(query) {

        interface OnJoinSelectedListener {
            fun onJoinSelected(home : DocumentSnapshot)
        }

    // TODO: switch to appropriate row binding
    inner class ViewHolder(private val binding: MenuRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

            fun bind(snapshot: DocumentSnapshot,
                     listener: OnJoinSelectedListener) {
                TODO("Bind appropriate fields + buttons")
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MenuRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }
}