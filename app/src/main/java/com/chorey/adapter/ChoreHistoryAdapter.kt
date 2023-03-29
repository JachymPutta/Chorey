package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ChoreModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.Calendar

open class ChoreHistoryAdapter(query: Query,
                               private val listener: OnHistorySelectedListener,
                               private val user : LoggedUserModel)
    : FirestoreAdapter<ChoreHistoryAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnHistorySelectedListener {
        fun onHistorySelected(chore : DocumentSnapshot)
    }

    //TODO Create its own row XML file
    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
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
                binding.choreName.text = choreModel.choreName
                binding.choreAssignee.text = choreModel.curAssignee
                if (choreModel.isTimed) {
                    binding.choreDueDate.text = date
                } else {
                    binding.choreDueDate.visibility = View.GONE
                    binding.choreDueText.visibility = View.GONE
                }

                // Display the history detail
                binding.root.setOnClickListener { listener.onHistorySelected((snapshot)) }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }
}