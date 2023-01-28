package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.chorey.data.NoteModel
import com.chorey.databinding.DialogNoteDetailBinding
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class NoteDetailDialog : DialogFragment(){
    private var _binding: DialogNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val args : NoteDetailDialogArgs by navArgs()
    private val viewModel by activityViewModels<LoginViewModel>()

    private lateinit var state: State

    enum class State {
        CREATE, EDIT, VIEW
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = if (args.noteModel == null) { State.CREATE } else { State.VIEW }
        changeUI(state)

        //TODO bindings go here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onEditClicked() {
        state = State.EDIT
        changeUI(state)
    }

    private fun onCreateClicked() {
        val uid = args.noteModel?.UID ?: UUID.randomUUID().toString()
        val note = NoteModel(
            UID = uid,
            //TODO: Get the text from the input - CHECK IF EMPTY
            note = "Hello",
            author = viewModel.user!!.name
        )

        Firebase.firestore.collection("homes").document(args.homeModel.UID)
            .collection("notes").document(uid).set(note)
    }

    private fun changeUI(state: State) {
        when(state) {
            State.CREATE -> {
                // Logical Changes
                // Visual Changes
            }
            State.VIEW -> {
                // Logical Changes
                // Visual Changes
            }
            State.EDIT -> {
                // Logical Changes
                // Visual Changes
            }
        }
    }

    companion object {
        const val TAG = "NoteDetailDialog"
    }
}