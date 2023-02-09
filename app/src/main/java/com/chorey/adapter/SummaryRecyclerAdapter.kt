package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeUserModel
import com.chorey.databinding.SummaryRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class SummaryRecyclerAdapter(query : Query, private val listener: OnSummarySelectedListener) :
    FirestoreAdapter<SummaryRecyclerAdapter.ViewHolder>(query) {

    private lateinit var homeUserModel : HomeUserModel

    interface OnSummarySelectedListener {
        fun onSummarySelected(summary : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: SummaryRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(snapshot: DocumentSnapshot, listener: OnSummarySelectedListener?) {
                homeUserModel = snapshot.toObject<HomeUserModel>() ?: return

                binding.summaryCardUserName.text = homeUserModel.name
                binding.summaryCardUserValue.text = homeUserModel.points.toString()

                binding.root.setOnClickListener { listener?.onSummarySelected(snapshot) }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SummaryRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

}