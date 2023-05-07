package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ExpenseModel
import com.chorey.databinding.ExpenseRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class ExpenseRecyclerAdapter(
    query: Query,
    private val listener: OnExpenseSelectedListener)
    : FirestoreAdapter<ExpenseRecyclerAdapter.ViewHolder>(query){
    private lateinit var expense: ExpenseModel

    interface OnExpenseSelectedListener {
        fun onExpenseSelected(expense : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: ExpenseRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(snapshot: DocumentSnapshot,
                 listener: OnExpenseSelectedListener) {

            expense = snapshot.toObject<ExpenseModel>() ?: return

            binding.root.setOnClickListener { listener.onExpenseSelected(snapshot) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ExpenseRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),
        parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    companion object {
        const val TAG = "ExpenseRecyclerAdapter"
    }
}