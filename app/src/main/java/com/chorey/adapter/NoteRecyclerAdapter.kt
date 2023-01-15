package com.chorey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.NoteModel
import com.chorey.databinding.NoteRecyclerRowBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

open class NoteRecyclerAdapter(query : Query, private val listener: OnNoteSelectedListener) :
    FirestoreAdapter<NoteRecyclerAdapter.ViewHolder>(query) {

    private lateinit var noteModel : NoteModel

    interface OnNoteSelectedListener {
        fun onNoteSelected(note : DocumentSnapshot)
    }

    inner class ViewHolder(private val binding: NoteRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(snapshot: DocumentSnapshot, listener: OnNoteSelectedListener?) {
                noteModel = snapshot.toObject<NoteModel>() ?: return

                // Add buttons for editing
                binding.noteRecyclerContent.text = noteModel.note

                binding.root.setOnClickListener { listener?.onNoteSelected(snapshot) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NoteRecyclerRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    companion object {
        const val TAG = "NoteRecyclerAdapter"
    }

}