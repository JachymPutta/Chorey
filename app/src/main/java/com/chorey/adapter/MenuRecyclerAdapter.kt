package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.HomeModel
import com.chorey.databinding.MenuRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class MenuRecyclerAdapter(query: Query, private val listener: OnHomeSelectedListener)
    : FirestoreAdapter<MenuRecyclerAdapter.ViewHolder>(query) {

    interface OnHomeSelectedListener {
        fun onHomeSelected(home : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: MenuRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind (snapshot: DocumentSnapshot,
            listener: OnHomeSelectedListener?) {
            val home = snapshot.toObject<HomeModel>() ?: return

            binding.homesListIcon.setImageResource(R.drawable.baseline_home_24)
            binding.homesListName.text = home.homeName

            binding.root.setOnClickListener {
                listener?.onHomeSelected(snapshot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MenuRecyclerRowBinding.
            inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

}