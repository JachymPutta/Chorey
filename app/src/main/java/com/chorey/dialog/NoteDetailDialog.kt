package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.HOME_COL
import com.chorey.NOTE_COL
import com.chorey.R
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.NoteModel
import com.chorey.databinding.DialogNoteDetailBinding
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class NoteDetailDialog(private val homeModel: HomeModel,
                       private val noteModel: NoteModel?,
                       private val state: DialogState) : DialogFragment(){
    private var _binding: DialogNoteDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<UserViewModel>()

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

        changeUI(state)

        binding.noteDetailCancelButton.setOnClickListener { dismiss() }
        binding.noteDetailRemoveButton.setOnClickListener { onRemoveClicked() }
        binding.noteDetailModifyButton.setOnClickListener { onCreateClicked() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onRemoveClicked() {
        val uid = noteModel!!.UID

        Firebase.firestore.collection(HOME_COL).document(homeModel.UID)
            .collection(NOTE_COL).document(uid).delete()
            .addOnSuccessListener { Log.d(TAG, "Note successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

        dismiss()
    }

    private fun onCreateClicked() {
        val uid = noteModel?.UID ?: UUID.randomUUID().toString()

        if(checkEmpty()) {
            Toast.makeText(requireContext(),
                "Note cannot be empty.",
                Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.noteDetailTextInput.editText?.text.toString()

        val note = NoteModel(
            UID = uid,
            note = text,
            author = viewModel.user!!.name
        )

        Firebase.firestore.collection(HOME_COL).document(homeModel.UID)
            .collection(NOTE_COL).document(uid).set(note)

        dismiss()
    }

    private fun checkEmpty() = binding.noteDetailTextInput.editText!!.text.isNullOrBlank()


    private fun changeUI(state: DialogState) {
        when(state) {
            DialogState.CREATE -> {
                binding.noteDetailRemoveButton.visibility = View.GONE
            }
            DialogState.EDIT -> {
                binding.noteDetailRemoveButton.visibility = View.VISIBLE
                binding.noteDetailTitle.setText(R.string.note_detail_title_edit)
                binding.noteDetailModifyButton.setText(R.string.note_detail_edit_button)
                binding.noteDetailTextInput.editText!!.setText(noteModel!!.note)
            }
        }
    }

    companion object {
        const val TAG = "NoteDetailDialog"
    }
}