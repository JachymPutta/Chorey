package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.HOME_COL
import com.chorey.USER_COL
import com.chorey.data.HomeModel
import com.chorey.data.InviteModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.JoinHomeRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

open class JoinHomeRecyclerAdapter(query: Query, private val listener: OnJoinSelectedListener) :
    FirestoreAdapter<JoinHomeRecyclerAdapter.ViewHolder>(query) {

    private lateinit var inviteModel : InviteModel

        interface OnJoinSelectedListener {
            fun onJoinSelected(invite : DocumentSnapshot)
        }

    inner class ViewHolder(private val binding: JoinHomeRecyclerRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

            fun bind(snapshot: DocumentSnapshot,
                     listener: OnJoinSelectedListener?) {
                inviteModel = snapshot.toObject<InviteModel>() ?: return

                binding.joinHomeHomeName.text = inviteModel.homeName
                binding.joinHomeSentByName.text = inviteModel.fromUser

                binding.joinHomeAcceptButton.setOnClickListener {
                    listener?.onJoinSelected(snapshot)
                    snapshot.reference.delete()
                }
                binding.joinHomeDeclineInvite.setOnClickListener { onDeclineInvite(snapshot) }

            }
    }

    private fun onDeclineInvite(snapshot: DocumentSnapshot) {
        snapshot.reference.delete()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoinHomeRecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    companion object {
        const val TAG = "JoinHomeRecyclerAdapter"
    }
}