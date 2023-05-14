package com.chorey.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.HomeUserModel
import com.chorey.databinding.UserPickerRowBinding

class UserPickerAdapter (
    private val userList: ArrayList<HomeUserModel>,
    private val listener: UserPickerListener,
    private val context: Context
    )
    : RecyclerView.Adapter<UserPickerAdapter.ViewHolder>() {

    interface UserPickerListener {
        fun onUserSelected(homeUserModel: HomeUserModel)
    }

    inner class ViewHolder(private val binding: UserPickerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor") //Can't get resources in adapter - would need to pass it in
        fun bind(user: HomeUserModel, listener: UserPickerListener?) {
            binding.icon.setImageResource(user.icon)
            binding.userName.text = user.name

            if (user.picked) {
                binding.userPickerLayout.background.
                setTint(ContextCompat.getColor(context, R.color.secondary_color))
            }

            binding.root.setOnClickListener {
                listener?.onUserSelected(user)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : UserPickerAdapter.ViewHolder {
        return ViewHolder(
            UserPickerRowBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user, listener)
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}