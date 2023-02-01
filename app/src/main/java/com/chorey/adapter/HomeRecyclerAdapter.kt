package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.ChoreModel
import com.chorey.data.RepeatInterval
import com.chorey.data.UserModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.chorey.util.ChoreUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

/**
 * Handles the list of all chores in each of the homes.
 * @param query : DB query to get all the chores for a home
 */
open class HomeRecyclerAdapter(query: Query,
                               private val listener: OnChoreSelectedListener,
                               private val user : UserModel)
    : FirestoreAdapter<HomeRecyclerAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnChoreSelectedListener {
        fun onChoreSelected(chore : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot,
                listener: OnChoreSelectedListener) {

            choreModel = snapshot.toObject<ChoreModel>() ?: return

            // Bind visuals
            binding.choreName.text = choreModel.choreName
            binding.choreAssignee.text = choreModel.curAssignee
            if (choreModel.isTimed) {
                binding.choreDueDate.text = choreModel.whenDue.toString()
            } else {
                binding.choreDueDate.visibility = View.GONE
                binding.choreDueText.visibility = View.GONE
            }

            // Bind logic
            binding.choreDoneButton.setOnClickListener { onDoneClicked(snapshot) }
            binding.root.setOnClickListener { listener.onChoreSelected(snapshot) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }

    private fun onDoneClicked(snapshot: DocumentSnapshot) {
        // TODO: this needs to update the points, the user assigned etc
        //TODO: update the db
        val lastCompleted = choreModel.curAssignee
        val newChore = ChoreUtil.updateData(choreModel.copy(), user)


        // Update the home variables
        val homeRef = Firebase.firestore.collection("homes").document(choreModel.homeId)
        val choreRef = homeRef.collection("chores").document(choreModel.UID)

        // Write the stuff in a batch
        Firebase.firestore.runBatch {

            // Handle points
            it.update(homeRef, "points", user.points)

            // If non repeating -> delete, else update due date
            when(choreModel.repeatsEvery) {
                RepeatInterval.None -> it.delete(snapshot.reference)
                else ->  it.set(choreRef, newChore)
            }
        }
    }

}