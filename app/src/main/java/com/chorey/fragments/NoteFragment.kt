package com.chorey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.adapter.NoteRecyclerAdapter
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.NoteModel
import com.chorey.databinding.FragmentNotesBinding
import com.chorey.dialog.NoteDetailDialog
import com.chorey.util.HomeUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class NoteFragment : Fragment(),
    EventListener<DocumentSnapshot>,
    NoteRecyclerAdapter.OnNoteSelectedListener
{
    private lateinit var home : HomeModel
    private lateinit var query: Query

    private lateinit var noteAdapter: NoteRecyclerAdapter
    private lateinit var binding: FragmentNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = HomeUtil.getHomeFromArgs(requireArguments())
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeRef = Firebase.firestore.collection(HOME_COL).document(home.homeUID)
        query = homeRef.collection(NOTE_COL)

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

        binding.allChoresRecycler.adapter = noteAdapter
        binding.allChoresRecycler.layoutManager = GridLayoutManager(requireView().context, NOTE_COLUMN_CNT)
    }

    override fun onStart() {
        super.onStart()
        noteAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        noteAdapter.stopListening()
    }

    private fun addNoteHandle() {
        NoteDetailDialog.newInstance(home, null, DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        //TODO: not sure what this should do

    }

    override fun onNoteSelected(note: DocumentSnapshot) {
        val noteModel = note.toObject<NoteModel>()

        NoteDetailDialog.newInstance(home, noteModel, DialogState.EDIT)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    companion object {
        const val TAG = "NoteFragment"
        const val NOTE_COLUMN_CNT = 2
        fun newInstance(home : HomeModel): NoteFragment{
            val fragment = NoteFragment()
            val args = HomeUtil.addHomeToArgs(Bundle(), home)
            fragment.arguments = args
            return fragment
        }
    }
}
