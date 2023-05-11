package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.databinding.UserIconRowBinding

class IconPickerAdapter (
    private val iconList: List<Int>,
    private val listener: IconPickerDialogListener)
    : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

    interface IconPickerDialogListener {
        fun onIconSelected(icon : Int)
    }

    inner class ViewHolder(private val binding: UserIconRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(icon: Int, listener: IconPickerDialogListener?) {
            binding.icon.setImageResource(icon)
            binding.root.setOnClickListener {
                listener?.onIconSelected(icon)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : IconPickerAdapter.ViewHolder {
        return ViewHolder(UserIconRowBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val icon = iconList[position]
        holder.bind(icon, listener)
    }

    override fun getItemCount(): Int {
        return iconList.size
    }
}
