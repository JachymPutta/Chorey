package com.chorey.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.chorey.CHORE_COL
import com.chorey.HOME_COL
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Random
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

    private val dueTime = Calendar.getInstance()
    private var timeChanged = false

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

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.choreDetailAssignedTo.setOnClickListener { onAssignClicked() }
        binding.createChoreRemoveButton.setOnClickListener { onRemoveClicked() }

        // TODO: this needs to get disabled when viewing
        binding.choreDetailDueDate.setOnClickListener { onDatePickerClicked() }
        binding.choreDetailDueTime.setOnClickListener { onTimePickerClicked() }

        // Hook up spinners
        val repeatAdapter = ArrayAdapter(requireContext(), R.layout.chore_spinner_item, RepeatInterval.values())
        val timedAdapter = ArrayAdapter(requireContext(), R.layout.chore_spinner_item, arrayOf("No", "Yes"))
        repeatAdapter.setDropDownViewResource(R.layout.chore_spinner_dropdown)
        timedAdapter.setDropDownViewResource(R.layout.chore_spinner_dropdown)
        binding.choreIntervalSpinner.adapter = repeatAdapter
        binding.choreDetailIsTimedBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleTimeUI(VISIBLE)
            } else {
                toggleTimeUI(GONE)
            }
        }

        binding.choreDetailMinsToComplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    binding.choreDetailPoints.text = "0"
                } else {
                    binding.choreDetailPoints.text = ChoreUtil.getPoints(s.toString().toInt()).toString()

                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })


        binding.createChoreNameInput.editText!!.setOnKeyListener { _, keyCode, event ->
            (event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)
        }
        toggleTimeUI(GONE)
        changeUI(state)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
                val date = String.format("${dueTime.get(Calendar.YEAR)}" +
                            "-${dueTime.get(Calendar.MONTH) + 1}" +
                            "-${dueTime.get(Calendar.DAY_OF_MONTH)}")
                binding.choreDetailDueDate.text = date
            }
            State.VIEW -> {
                val choreModel = args.choreModel!!

                // Update the chore timings
                if (choreModel.isTimed) {
                    val lastDue = Calendar.getInstance()
                    lastDue.timeInMillis = choreModel.whenDue
                    val id = RepeatInterval.values().indexOf(choreModel.repeatsEvery)
                    binding.choreDetailDueDate.text = Date(choreModel.whenDue).toString()
                    binding.choreDetailDueTime.text = getTimeFormat(requireContext()).format(lastDue.time)
                    binding.choreIntervalSpinner.setSelection(id)
                    binding.choreDetailIsTimedBox.isChecked = true

                    toggleTimeUI(VISIBLE)
                } else {
                    toggleTimeUI(GONE)
                }

                // Fill in existing data
                binding.createChoreNameInput.editText!!.setText(choreModel.choreName)
                binding.choreDetailAssignedTo.text = choreModel.assignedTo.joinToString()
                binding.choreDetailMinsToComplete.setText(choreModel.timeToComplete.toString())
                binding.choreDetailPoints.text = choreModel.points.toString()

                // Visual Changes
                binding.createChoreRemoveButton.visibility = VISIBLE
                binding.createChoreCreateButton.setText(R.string.create_chore_edit_button)

                // Logical Changes
                binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }
            }
        }
    }

    /***********************************************************************************************
     * Timing stuff
     **********************************************************************************************/
    private fun onDatePickerClicked() {
        val mYear = dueTime.get(Calendar.YEAR)
        val mMonth = dueTime.get(Calendar.MONTH)
        val mDay = dueTime.get(Calendar.DAY_OF_MONTH)


       DatePickerDialog(requireContext(), this, mYear, mMonth, mDay).show()
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Months start at 0 for some reason
        val displayMonth = month + 1
        val date = String.format("$year-$displayMonth-$dayOfMonth")
        binding.choreDetailDueDate.text = date

        dueTime.set(Calendar.YEAR, year)
        dueTime.set(Calendar.MONTH, month)
        dueTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        timeChanged = true
    }

    private fun onTimePickerClicked() {
        val hour = dueTime.get(Calendar.HOUR_OF_DAY)
        val minute = dueTime.get(Calendar.MINUTE)

        picker = TIME_PICKER
        TimePickerDialog(activity, this, hour, minute, is24HourFormat(activity)).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeString = String.format("$hourOfDay:$minute")

        // This is here so I know how to add multiple time pickers if I need
        if (picker == TIME_PICKER) {
            binding.choreDetailDueTime.text = timeString
            dueTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dueTime.set(Calendar.MINUTE, minute)
            timeChanged = true
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

        if (!checkChoreInput()) return

        val uid = args.choreModel?.UID ?: UUID.randomUUID().toString()
        val choreName = binding.createChoreNameInput.editText?.text.toString()
        val timeToComplete = binding.choreDetailMinsToComplete.text.toString().toInt()
        // We only update the time if a different one is selected
        val whenDue = if (timeChanged) dueTime.timeInMillis
                        else args.choreModel?.whenDue ?: Long.MAX_VALUE

        val choreModel = ChoreModel(
            UID = uid,
            choreName = choreName,
            homeId = args.homeModel.UID,
            assignedTo = assignedTo,
            curAssignee = assignedTo.random(),
            repeatsEvery = binding.choreIntervalSpinner.selectedItem as RepeatInterval,
            timeToComplete = timeToComplete,
            points = ChoreUtil.getPoints(timeToComplete),
            whenDue = whenDue,
            isTimed = binding.choreDetailIsTimedBox.isChecked
        )

        Firebase.firestore.collection(HOME_COL).document(args.homeModel.UID)
            .collection(CHORE_COL).document(choreModel.UID).set(choreModel)

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private fun onRemoveClicked() {
        val uid = args.choreModel!!.UID

        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Are you sure you want to remove this chore?")
            .setPositiveButton(R.string.confirm_remove_yes)
            { _, _ ->
                Firebase.firestore.collection(HOME_COL).document(args.homeModel.UID)
                    .collection(CHORE_COL).document(uid).delete()
                    .addOnSuccessListener { Log.d(TAG, "Chore successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                dismiss()
            }
            .setNegativeButton(R.string.confirm_remove_no)
            { a, _ ->
               a.dismiss()
            }
        builder.show()
    }

    private fun checkChoreInput() : Boolean {
        return if (!binding.createChoreNameInput.editText!!.text.isNullOrBlank() &&
                    !binding.choreDetailAssignedTo.text.isNullOrBlank() &&
                    !binding.choreDetailMinsToComplete.text.isNullOrBlank()) {
            true
        } else {
            Toast.makeText(requireContext(),
                "Please fill in all the fields.",
                     Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun toggleTimeUI(state : Int) {
        binding.choreDetailDueText.visibility = state
        binding.choreDetailDueDate.visibility = state
        binding.choreDetailAt.visibility = state
        binding.choreDetailDueTime.visibility = state
        binding.choreRepeatText.visibility = state
        binding.choreIntervalSpinner.visibility = state
    }


    companion object {
        const val TAG = "CreateChoreDialog"
        const val TIME_PICKER = 1
    }



}