package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeModel
import com.chorey.databinding.MenuRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class MenuRecyclerViewAdapter(query: Query, private val listener: OnHomeSelectedListener)
    : FirestoreAdapter<MenuRecyclerViewAdapter.ViewHolder>(query) {

    interface OnHomeSelectedListener {
        fun onHomeSelected(home : DocumentSnapshot)
    }

    inner class ViewHolder(val binding: MenuRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind (
            snapshot: DocumentSnapshot,
            listener: OnHomeSelectedListener?
        ) {

            val home = snapshot.toObject<HomeModel>() ?: return
            val resources = binding.root.resources

            binding.homesListIcon.setImageResource(com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_dark_normal)
            binding.homesListName.text = home.homeName
            binding.homesListMembers.text = "Members"
            binding.homesListPoints.text = "Points"

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