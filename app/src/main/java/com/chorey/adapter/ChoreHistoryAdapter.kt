package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.DATE_TIME_PATTERN
import com.chorey.data.ChoreModel
import com.chorey.databinding.HistoryRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                val dateFormat = SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault())
                val dateTime = Date(choreModel.whenDue)
                val formattedDateTime = dateFormat.format(dateTime)
                // Bind visuals
                binding.historyName.text = choreModel.choreName
                binding.historyAssignee.text = choreModel.curAssignee
                if (choreModel.isTimed) {
                    binding.historyDueDate.text = formattedDateTime
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