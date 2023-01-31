package com.chorey.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.chorey.R
import com.chorey.data.ChoreModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogChoreDetailBinding
import com.chorey.util.ChoreUtil
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.UUID

class ChoreDetailDialog : DialogFragment(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private var _binding: DialogChoreDetailBinding? = null
    private val binding get() = _binding!!
    private val args : ChoreDetailDialogArgs by navArgs()
    private var picker = TIME_PICKER

    private lateinit var state: State
    private lateinit var assignedTo: ArrayList<String>

    private val dueDatetime = arrayOf(0,0,0,0,0)
    private val timeToComplete = arrayOf(0,0)

    enum class State {
        CREATE, VIEW
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

        assignedTo = args.choreModel?.assignedTo ?: arrayListOf()
        state = if (args.choreModel == null) { State.CREATE } else { State.VIEW }
        changeUI(state)

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.choreDetailAssignedTo.setOnClickListener { onAssignClicked() }
        binding.createChoreRemoveButton.setOnClickListener { onRemoveClicked() }

        // TODO: this needs to get disabled when viewing
        binding.choreDetailDueDate.setOnClickListener { onDatePickerClicked() }
        binding.choreDetailDueTime.setOnClickListener { onTimePickerClicked() }
        binding.choreDetailCompleteTime.setOnClickListener { onCompleteTimeClicked() }

        // Hook up spinner
        val repeatAdapter = ArrayAdapter(requireContext(), R.layout.chore_spinner_item, RepeatInterval.values())
        repeatAdapter.setDropDownViewResource(R.layout.chore_spinner_dropdown)
        binding.choreRepeatSpinner.adapter = repeatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeUI(state: State) {
        // TODO - the input fields need to be disabled -> global variable?
        when(state) {
            State.CREATE -> {
                // Logical Changes
                binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }

                // Visual Changes
                binding.createChoreCreateButton.setText(R.string.create_home_yes)
                binding.createChoreRemoveButton.visibility = GONE
            }
            State.VIEW -> {
                val choreModel = args.choreModel!!
                // Logical Changes
                binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }

                // Fill in existing data
                binding.createChoreNameInput.editText!!.setText(choreModel.choreName)
                binding.choreDetailAssignedTo.text = choreModel.assignedTo.joinToString()

                // Visual Changes
                binding.createChoreRemoveButton.visibility = VISIBLE
                binding.createChoreCreateButton.setText(R.string.create_chore_edit_button)
            }
        }
    }

    /***********************************************************************************************
     * Timing stuff
     **********************************************************************************************/
    private fun onDatePickerClicked() {
        val c = Calendar.getInstance();
        val mYear = c.get(Calendar.YEAR);
        val mMonth = c.get(Calendar.MONTH);
        val mDay = c.get(Calendar.DAY_OF_MONTH);

       DatePickerDialog(requireContext(), this, mYear, mMonth, mDay).show()
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Months start at 0 for some reason
        val displayMonth = month + 1
        val date = String.format("$year-$displayMonth-$dayOfMonth")
        binding.choreDetailDueDate.text = date
        dueDatetime[0] = year
        dueDatetime[1] = month
        dueDatetime[2] = dayOfMonth
    }

    private fun onTimePickerClicked() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        picker = TIME_PICKER
        TimePickerDialog(activity, this, hour, minute, is24HourFormat(activity)).show()
    }

    private fun onCompleteTimeClicked() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        picker = COMPLETE_PICKER
        val dialog = TimePickerDialog(activity, this, hour, minute, true)
        dialog.setTitle("Time to complete the task")
        dialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val time = String.format("$hourOfDay:$minute")

        if (picker == TIME_PICKER) {
            binding.choreDetailDueTime.text = time
            dueDatetime[3] = hourOfDay
            dueDatetime[4] = minute
        } else if (picker == COMPLETE_PICKER) {
            binding.choreDetailCompleteTime.text = time
            binding.choreDetailPoints.text = time
            timeToComplete[0] = hourOfDay
            timeToComplete[1] = minute
        }
    }
    /**********************************************************************************************/

    /**
     * Build the selection menu, based on the users in the home
     */
    private fun onAssignClicked() {
        val builder = AlertDialog.Builder(requireContext())
        val userArray : Array<String> = args.homeModel.users.toTypedArray()
        val selectedUsers : ArrayList<String> = arrayListOf()
        val selectionArray = BooleanArray(args.homeModel.users.size)

        builder.setTitle(R.string.chore_detail_assign_hint)
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
            .setOnCancelListener {
                binding.choreDetailAssignedTo.text = selectedUsers.joinToString()
                assignedTo = selectedUsers
            }

        builder.show()
    }

    private fun onCreateClicked() {
        val user = Firebase.auth.currentUser

        user?.let {
            // Take existing or create new
            val uid = args.choreModel?.UID ?: UUID.randomUUID().toString()

            // TODO: this needs to return out of the function
            checkChoreInput()
            // TODO: Need to assemble the times and convert them to a long for storage

            val choreModel = ChoreModel(
                UID = uid,
                choreName = binding.createChoreNameInput.editText?.text.toString(),
                homeId = args.homeModel.UID,
                assignedTo = assignedTo,
                points = ChoreUtil.getPoints(timeToComplete[0], timeToComplete[1])
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
        const val TIME_PICKER = 1
        const val COMPLETE_PICKER = 2
    }



}