package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ContribModel
import com.chorey.databinding.ContribRecyclerRowBinding

class ContribRecyclerAdapter(private val contributors: ArrayList<ContribModel>)
    : RecyclerView.Adapter<ContribRecyclerAdapter.ViewHolder>() {

   inner class ViewHolder(private val binding: ContribRecyclerRowBinding)
       : RecyclerView.ViewHolder(binding.root) {
           fun bind(position: Int) {
               val contrib = contributors[position]
               val contribText = "+ ${contrib.amount}"

               binding.contribCardName.text = contrib.contributor
               binding.contribCardValue.text = contribText
           }
       }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ContribRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return contributors.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }


}