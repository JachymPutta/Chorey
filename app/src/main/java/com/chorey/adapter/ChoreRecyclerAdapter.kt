package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.CHORE_COL
import com.chorey.HOME_COL
import com.chorey.USER_COL
import com.chorey.data.ChoreModel
import com.chorey.data.RepeatInterval
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.HomeRecyclerRowBinding
import com.chorey.util.ChoreUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.Calendar

/**
 * Handles the list of all chores in each of the homes.
 * @param query : DB query to get all the chores for a home
 */
open class ChoreRecyclerAdapter(query: Query,
                                private val listener: OnChoreSelectedListener,
                                private val user : LoggedUserModel)
    : FirestoreAdapter<ChoreRecyclerAdapter.ViewHolder>(query) {
    private lateinit var choreModel: ChoreModel

    interface OnChoreSelectedListener {
        fun onChoreSelected(chore : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: HomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot,
                listener: OnChoreSelectedListener) {

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
        val newChore = ChoreUtil.updateData(choreModel.copy(), user)
        val homeRef = Firebase.firestore.collection(HOME_COL).document(choreModel.homeId)
        val choreRef = homeRef.collection(CHORE_COL).document(choreModel.UID)
        val userRef = homeRef.collection(USER_COL).document(user.name)

        // Write the stuff in a batch
        Firebase.firestore.runBatch {

            // Handle points
            it.update(userRef, LoggedUserModel.FIELD_POINTS, FieldValue.increment(choreModel.points.toLong()))

            //TODO: don't delete the chores, but mark them as completed
            // If non repeating -> delete, else update due date
            when(choreModel.repeatsEvery) {
                RepeatInterval.None -> it.delete(snapshot.reference)
                else ->  it.set(choreRef, newChore)
            }
        }
    }

}