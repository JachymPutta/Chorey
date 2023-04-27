package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ChoreModel
import com.chorey.databinding.HistoryRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.Calendar

open class ChoreHistoryAdapter(query: Query,
                               private val listener: OnHistorySelectedListener)
    : FirestoreAdapter<ChoreHistoryAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnHistorySelectedListener {
        fun onHistorySelected(chore : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: HistoryRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(snapshot: DocumentSnapshot,
                     listener: OnHistorySelectedListener) {
                choreModel = snapshot.toObject<ChoreModel>() ?: return

                val lastDue = Calendar.getInstance()
                lastDue.timeInMillis = choreModel.whenDue
                val date = String.format("${lastDue.get(Calendar.YEAR)}" +
                        "-${lastDue.get(Calendar.MONTH) + 1}" +
                        "-${lastDue.get(Calendar.DAY_OF_MONTH)}" +
                        " ${lastDue.get(Calendar.HOUR_OF_DAY)}")

                // Bind visuals
                binding.historyName.text = choreModel.choreName
                binding.historyAssignee.text = choreModel.curAssignee
                if (choreModel.isTimed) {
                    binding.historyDueDate.text = date
                } else {
                    binding.historyDueDate.visibility = View.GONE
                    binding.historyDueText.visibility = View.GONE
                }

                // Display the history detail
                binding.root.setOnClickListener { listener.onHistorySelected((snapshot)) }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }
}