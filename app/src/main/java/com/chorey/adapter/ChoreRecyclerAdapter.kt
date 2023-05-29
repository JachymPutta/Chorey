package com.chorey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.chorey.CHORE_COL
import com.chorey.DATE_TIME_PATTERN
import com.chorey.HISTORY_COL
import com.chorey.HOME_COL
import com.chorey.USER_COL
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.data.RepeatInterval
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.ChoreRecyclerRowBinding
import com.chorey.util.ChoreUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        fun onChoreDone(users : ArrayList<HomeUserModel>)
    }

    inner class ViewHolder(private val binding: ChoreRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot,
                listener: OnChoreSelectedListener) {

            choreModel = snapshot.toObject<ChoreModel>() ?: return

            val dateFormat = SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault())
            val dateTime = Date(choreModel.whenDue)
            val formattedDateTime = dateFormat.format(dateTime)

            // Bind visuals
            binding.choreName.text = choreModel.choreName
            binding.choreAssignee.text = choreModel.curAssignee
            if (choreModel.isTimed) {
                binding.choreDueDate.text = formattedDateTime
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
        return ViewHolder(ChoreRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(getSnapshot(pos), listener)
    }

    private fun onDoneClicked(snapshot: DocumentSnapshot) {
        val curChore = snapshot.toObject<ChoreModel>() ?: return

        val homeRef = Firebase.firestore.collection(HOME_COL).document(curChore.homeId)
        val oldChoreRef = homeRef.collection(CHORE_COL).document(curChore.choreUID)
        val historyRef = homeRef.collection(HISTORY_COL).document(curChore.choreUID)
        val users = arrayListOf<HomeUserModel>()

        // Write the stuff in a batch
        Firebase.firestore.runTransaction {

            // Handle points
            val home = it.get(homeRef).toObject<HomeModel>()!!
            home.users.forEach { model ->
                if (model.name == user.name) {
                    model.points += curChore.points
                }
                users.add(model)
            }
            it.update(homeRef, HomeModel.FIELD_USERS, home.users)

            it.delete(oldChoreRef)
            when(curChore.repeatsEvery) {
                RepeatInterval.None -> {}
                else ->  {
                    val newChore = ChoreUtil.updateData(curChore.copy(), user)
                    val newChoreRef = homeRef.collection(CHORE_COL).document(newChore.choreUID)
                    it.set(newChoreRef, newChore)
                }
            }

            ChoreUtil.completeChore(curChore, user)

            it.set(historyRef, curChore)
        }

        // Update points in the local view model
        listener.onChoreDone(users)
    }

}