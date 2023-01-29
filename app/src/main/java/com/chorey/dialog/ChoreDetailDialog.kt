package com.chorey.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.data.ChoreTemplate
import com.chorey.databinding.DialogChoreDetailBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class ChoreDetailDialog : DialogFragment() {
    private var _binding: DialogChoreDetailBinding? = null
    private val binding get() = _binding!!
    private val args : ChoreDetailDialogArgs by navArgs()

    private lateinit var state: State
    private var assignedTo = arrayListOf<String>()

    enum class State {
        CREATE, EDIT, VIEW
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = if (args.choreModel == null) { State.CREATE } else { State.VIEW }
        changeUI(state)

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.choreDetailAssignedTo.setOnClickListener { onAssignClicked() }
        binding.createChoreRemoveButton.setOnClickListener { onRemoveClicked() }


        val adapter = ArrayAdapter(requireContext(), R.layout.chore_spinner_item, ChoreTemplate.values())
        adapter.setDropDownViewResource(R.layout.chore_spinner_dropdown)
        binding.choreTemplateSpinner.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeUI(state: State) {
        when(state) {
            State.CREATE -> {
                // Logical Changes
                binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }

                // Visual Changes
                binding.createChoreCreateButton.setText(R.string.create_home_yes)
                binding.createChoreRemoveButton.visibility = GONE
            }
            State.VIEW -> {
                // Logical Changes
                binding.createChoreNameInput.isFocusable = false
                binding.createChoreCreateButton.setOnClickListener { onEditClicked() }

                // Visual Changes
                //TODO: put the chore parameters into the fields
                binding.createChoreRemoveButton.visibility = VISIBLE
                binding.createChoreCreateButton.setText(R.string.create_chore_edit_button)
            }
            State.EDIT -> {
                // Logical Changes
                // TODO: this focusable doesn't do anything
                binding.createChoreNameInput.isFocusableInTouchMode = true
                binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }

                // Visual Changes
                binding.createChoreCreateButton.setText(R.string.create_chore_update_button)
            }
        }
    }

    private fun onEditClicked() {
        changeUI(State.EDIT)
    }

    /**
     * Build the selection menu, based on the users in the home
     */
    private fun onAssignClicked() {
        val builder = AlertDialog.Builder(requireContext())
        val userArray : Array<String> = args.homeModel.users.toTypedArray()
        val selectedUsers : ArrayList<String> = arrayListOf()
        val selectionArray = BooleanArray(args.homeModel.users.size)

        builder.setTitle(R.string.chore_detail_assign_hint)
            .setCancelable(false) //TODO: can just make it save the current selection
            .setMultiChoiceItems(userArray, selectionArray)
            { _ , which, isChecked ->
                if (isChecked) {
                    selectedUsers.add(userArray[which])
                } else if (selectedUsers.contains(userArray[which])) {
                    selectedUsers.remove(userArray[which])
                }
            }
            .setPositiveButton("Select") {_ , _ ->
                binding.choreDetailAssignedTo.text = selectedUsers.joinToString()
                assignedTo = selectedUsers
            }
            .setNeutralButton("Clear") { _, _ ->
                selectionArray.fill(false)
                selectedUsers.clear()
                binding.choreDetailAssignedTo.text = ""
            }
            .setNegativeButton("Cancel") { _, _ ->
            }

        builder.show()
    }

    private fun onCreateClicked() {
        val user = Firebase.auth.currentUser

        user?.let {
            // Take existing or create new
            val uid = args.choreModel?.UID ?: UUID.randomUUID().toString()

            checkChoreInput()

            val choreModel = ChoreModel(
                UID = uid,
                choreName = binding.createChoreNameInput.editText?.text.toString(),
                homeId = args.homeModel.UID,
                choreTemplate = binding.choreTemplateSpinner.selectedItem as ChoreTemplate,
                assignedTo = assignedTo
            )

            Firebase.firestore.collection("homes").document(args.homeModel.UID)
                .collection("chores").document(choreModel.UID).set(choreModel)
        }

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private fun onRemoveClicked() {
        val uid = args.choreModel!!.UID

        //TODO: I could show the confirm remove dialog with the chore name and the doc reference
        Firebase.firestore.collection("homes").document(args.homeModel.UID)
            .collection("chores").document(uid).delete()
            .addOnSuccessListener { Log.d(TAG, "Chore successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

        dismiss()

    }

    private fun checkChoreInput() {
        // TODO: Non-empty inputs etc
    }


    companion object {
        const val TAG = "CreateChoreDialog"
    }

}