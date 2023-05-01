package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chorey.R
import com.chorey.adapter.NoteRecyclerAdapter
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.NoteModel
import com.chorey.databinding.FragmentNotesBinding
import com.chorey.dialog.HomeDetailDialog
import com.chorey.dialog.NoteDetailDialog
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class NoteFragment(
    private val home : HomeModel,
    private val query: Query
) : Fragment(),
    EventListener<DocumentSnapshot>,
    NoteRecyclerAdapter.OnNoteSelectedListener
{
    private lateinit var noteAdapter: NoteRecyclerAdapter
    private lateinit var binding: FragmentNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteAdapter = object : NoteRecyclerAdapter(query, this@NoteFragment) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.noChoresLeftText.visibility = View.VISIBLE
                    binding.allChoresRecycler.visibility = View.GONE
                } else {
                    binding.noChoresLeftText.visibility = View.GONE
                    binding.allChoresRecycler.visibility = View.VISIBLE
                }
            }
        }

        binding.addChoreButton.setOnClickListener { addNoteHandle() }

        binding.homeRecyclerTitle.setText(R.string.home_notes_title)
        binding.noChoresLeftText.setText(R.string.home_no_notes_left)
        binding.noChoresLeftText.visibility = View.GONE


        binding.allChoresRecycler.adapter = noteAdapter
        binding.allChoresRecycler.layoutManager = GridLayoutManager(requireView().context, NOTE_COLUMN_CNT)
        noteAdapter.startListening()
    }

    private fun addNoteHandle() {
        NoteDetailDialog(homeModel = home, noteModel = null, state = DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        //TODO: not sure what this should do

    }

    override fun onNoteSelected(note: DocumentSnapshot) {
        val noteModel = note.toObject<NoteModel>()

        NoteDetailDialog(homeModel = home, noteModel = noteModel, state = DialogState.EDIT)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    companion object {
        const val TAG = "NoteFragment"
        const val NOTE_COLUMN_CNT = 2
    }
}
