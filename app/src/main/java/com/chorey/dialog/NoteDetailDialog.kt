package com.chorey.dialog

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.chorey.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class NoteDetailDialog : DialogFragment(){
    private var _binding: DialogNoteDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<UserViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    private lateinit var homeModel: HomeModel
    private var noteModel: NoteModel? = null
    private lateinit var state: DialogState

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeModel = homeViewModel.home.value!!
        state = DialogState.values()[arguments?.getInt("dialogState")!!]
        if (state == DialogState.EDIT) {
            noteModel = getNoteFromArgs(requireArguments())
        }

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
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Check if the touched view is not an input field or a view that should keep the keyboard open
                if (requireActivity().window.currentFocus !is TextInputLayout) {
                    // Hide the keyboard
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onRemoveClicked() {
        val uid = noteModel!!.noteUID

        Firebase.firestore.collection(HOME_COL).document(homeModel.homeUID)
            .collection(NOTE_COL).document(uid).delete()
            .addOnSuccessListener { Log.d(TAG, "Note successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

        dismiss()
    }

    private fun onCreateClicked() {
        val uid = noteModel?.noteUID ?: UUID.randomUUID().toString()

        if(checkEmpty()) {
            Toast.makeText(requireContext(),
                "Note cannot be empty.",
                Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.noteDetailTextInput.editText?.text.toString()

        val note = NoteModel(
            noteUID = uid,
            note = text,
            author = viewModel.user.value!!.name
        )

        Firebase.firestore.collection(HOME_COL).document(homeModel.homeUID)
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

    private fun getNoteFromArgs(arguments : Bundle) : NoteModel {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments.getParcelable(NoteModel.toString(), NoteModel::class.java)!!
        } else {
            val uid = arguments.getString(NoteModel.FIELD_UID)!!
            val note = arguments.getString(NoteModel.FIELD_NOTE)!!
            val author = arguments.getString(NoteModel.FIELD_AUTHOR)!!
            NoteModel(uid, note, author)
        }
    }

    companion object {
        const val TAG = "NoteDetailDialog"

        private fun addNoteToArgs(args : Bundle, noteModel: NoteModel) : Bundle {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.apply { putParcelable(NoteModel.toString(), noteModel) }
            } else {
                args.apply {
                    putString(NoteModel.FIELD_UID, noteModel.noteUID)
                    putString(NoteModel.FIELD_NOTE, noteModel.note)
                    putString(NoteModel.FIELD_AUTHOR, noteModel.author)
                }
            }
        }

        fun newInstance(noteModel: NoteModel?,
                        state: DialogState): NoteDetailDialog{
            val fragment = NoteDetailDialog()
            val args = Bundle()
            noteModel?.let { addNoteToArgs(args, it) }
            val id = DialogState.values().indexOf(state)
            args.putInt("dialogState", id)
            fragment.arguments = args
            return fragment
        }
    }
}