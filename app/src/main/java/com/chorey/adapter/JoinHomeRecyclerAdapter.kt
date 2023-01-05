package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeModel
import com.chorey.databinding.JoinHomeRecyclerRowBinding
import com.chorey.databinding.MenuRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class JoinHomeRecyclerAdapter(query: Query, private val listener: OnJoinSelectedListener) :
    FirestoreAdapter<JoinHomeRecyclerAdapter.ViewHolder>(query) {

        interface OnJoinSelectedListener {
            fun onJoinSelected(home : DocumentSnapshot)
        }

    // TODO: switch to appropriate row binding
    inner class ViewHolder(private val binding: JoinHomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

            fun bind(snapshot: DocumentSnapshot,
                     listener: OnJoinSelectedListener?) {
                val home = snapshot.toObject<HomeModel>() ?: return

                binding.joinHomeHomeName.text = home.homeName

                binding.root.setOnClickListener {
                    listener?.onJoinSelected(snapshot)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoinHomeRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }
}