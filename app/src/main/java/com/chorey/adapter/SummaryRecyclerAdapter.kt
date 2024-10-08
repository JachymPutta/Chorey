package com.chorey.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeUserModel
import com.chorey.databinding.SummaryRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class SummaryRecyclerAdapter(private val userList : ArrayList<HomeUserModel>) :
     RecyclerView.Adapter<SummaryRecyclerAdapter.ViewHolder>(){

    inner class ViewHolder(private val binding: SummaryRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(homeUserModel: HomeUserModel, position : Int) {
                val nameText = String.format("${position+1}. ${homeUserModel.name}")

                binding.summaryCardUserName.text = nameText
                binding.summaryCardUserValue.text = homeUserModel.points.toString()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SummaryRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user, position)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}