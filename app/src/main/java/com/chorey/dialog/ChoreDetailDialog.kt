package com.chorey.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.chorey.CHORE_COL
import com.chorey.DATE_PATTERN
import com.chorey.HOME_COL
import com.chorey.R
import com.chorey.TIME_PATTERN
import com.chorey.data.ChoreModel
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogChoreDetailBinding
import com.chorey.util.ChoreUtil
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChoreDetailDialog : DialogFragment(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private var _binding: DialogChoreDetailBinding? = null
    private val binding get() = _binding!!
    private var picker = TIME_PICKER

    private lateinit var assignedTo: ArrayList<String>

    private val dueTime = Calendar.getInstance()
    private var timeChanged = false

    private lateinit var homeModel : HomeModel
    private var choreModel: ChoreModel? = null
    private lateinit var state: DialogState

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeModel = homeViewModel.home.value!!
        state = DialogState.values()[arguments?.getInt("dialogState")!!]
        if (state == DialogState.EDIT) {
            choreModel = ChoreUtil.getChoreFromArgs(requireArguments())
        }
        _binding = DialogChoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assignedTo = choreModel?.assignedTo ?: arrayListOf()

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.choreDetailAssignedTo.setOnClickListener { onAssignClicked() }
        binding.choreDetailDueDate.setOnClickListener { onDatePickerClicked() }
        binding.choreDetailDueTime.setOnClickListener { onTimePickerClicked() }
        binding.createChoreRemoveButton.setOnClickListener { onRemoveClicked() }
        binding.createChoreCreateButton.setOnClickListener { onCreateClicked() }

        // Hook up spinners
        val repeatAdapter =
            ArrayAdapter(requireContext(), R.layout.repetition_spinner_item, RepeatInterval.values())
        repeatAdapter.setDropDownViewResource(R.layout.repetition_spinner_dropdown)
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
                    binding.choreDetailPoints.text =
                        ChoreUtil.getPoints(s.toString().toInt()).toString()

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
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
//            setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun changeUI(state: DialogState) {
        when (state) {
            DialogState.CREATE -> {
                // Visual Changes
                binding.createChoreCreateButton.setText(R.string.create_home_yes)
                binding.createChoreRemoveButton.visibility = GONE
                val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
                val dateTime = Date(dueTime.timeInMillis)
                val formattedDateTime = dateFormat.format(dateTime)
                binding.choreDetailDueDate.text = formattedDateTime
            }

            DialogState.EDIT -> {
                fillChoreData()

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
        val mYear = dueTime.get(Calendar.YEAR)
        val mMonth = dueTime.get(Calendar.MONTH)
        val mDay = dueTime.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            R.style.DatePickerDialog,
            this,
            mYear, mMonth, mDay
        ).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dueTime.set(Calendar.YEAR, year)
        dueTime.set(Calendar.MONTH, month)
        dueTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val formattedDateTime = dateFormat.format(dueTime.time)

        binding.choreDetailDueDate.text = formattedDateTime
        timeChanged = true
    }

    private fun onTimePickerClicked() {
        val hour = dueTime.get(Calendar.HOUR_OF_DAY)
        val minute = dueTime.get(Calendar.MINUTE)

        picker = TIME_PICKER
        TimePickerDialog(
            requireContext(),
            R.style.TimePickerDialog,
            this@ChoreDetailDialog,
            hour, minute, is24HourFormat(activity)
        ).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // This is here so I know how to add multiple time pickers if I need
        if (picker == TIME_PICKER) {
            dueTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dueTime.set(Calendar.MINUTE, minute)

            val timeFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
            val formattedTime = timeFormat.format(dueTime.time)

            binding.choreDetailDueTime.text = formattedTime
            timeChanged = true
        }
    }
    /**********************************************************************************************/

    /**
     * Build the selection menu, based on the users in the home
     */
    private fun onAssignClicked() {
        val builder = AlertDialog.Builder(requireContext())
//        val userArray: Array<String> = homeModel.users.toTypedArray()
        val userArray : Array<String> = arrayOf()
        val selectedUsers: ArrayList<String> = arrayListOf()
        val selectionArray = BooleanArray(homeModel.users.size)

        builder.setTitle(R.string.chore_detail_assign_hint)
            .setMultiChoiceItems(userArray, selectionArray)
            { _, which, isChecked ->
                if (isChecked) {
                    selectedUsers.add(userArray[which])
                } else if (selectedUsers.contains(userArray[which])) {
                    selectedUsers.remove(userArray[which])
                }
            }
            .setPositiveButton("Select") { _, _ ->
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

        val dialog = builder.create()
        //TODO: custom view?
//        dialog?.window?.setBackgroundDrawableResource(R.color.primaryColor)
        dialog.show()
    }

    private fun onCreateClicked() {

        if (!checkChoreInput()) return

        val uid = choreModel?.choreUID ?: UUID.randomUUID().toString()
        val choreName = binding.createChoreNameInput.editText?.text.toString()
        val timeToComplete = binding.choreDetailMinsToComplete.text.toString().toInt()
        // We only update the time if a different one is selected
        val whenDue = if (timeChanged) dueTime.timeInMillis
        else choreModel?.whenDue ?: Long.MAX_VALUE

        val choreModel = ChoreModel(
            choreUID = uid,
            choreName = choreName,
            homeId = homeModel.homeUID,
            assignedTo = assignedTo,
            curAssignee = assignedTo.random(),
            repeatsEvery = binding.choreIntervalSpinner.selectedItem as RepeatInterval,
            timeToComplete = timeToComplete,
            points = ChoreUtil.getPoints(timeToComplete),
            whenDue = whenDue,
            isTimed = binding.choreDetailIsTimedBox.isChecked
        )

        Log.d(TAG, "Creating chore $choreModel")
        Firebase.firestore.collection(HOME_COL).document(homeModel.homeUID)
            .collection(CHORE_COL).document(uid).set(choreModel)

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private fun onRemoveClicked() {
        Firebase.firestore.collection(HOME_COL).document(homeModel.homeUID)
                .collection(CHORE_COL).document(choreModel!!.choreUID).delete()

// TODO: this crashes app -- not sure why
//
//            .get().addOnSuccessListener { ds ->
//                ConfirmRemoveDialog().apply{
//                    snapshot = ds
//                    name = choreModel!!.choreName
//                }.show(childFragmentManager, ConfirmRemoveDialog.TAG)
//        }
        this.dismiss()
    }

    private fun checkChoreInput(): Boolean {
        return if (!binding.createChoreNameInput.editText!!.text.isNullOrBlank() &&
            !binding.choreDetailAssignedTo.text.isNullOrBlank() &&
            !binding.choreDetailMinsToComplete.text.isNullOrBlank()
        ) {
            true
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill in all the fields.",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    private fun toggleTimeUI(state: Int) {
        binding.choreDetailDueText.visibility = state
        binding.choreDetailDueDate.visibility = state
        binding.choreDetailAt.visibility = state
        binding.choreDetailDueTime.visibility = state
        binding.choreRepeatText.visibility = state
        binding.choreIntervalSpinner.visibility = state
    }

    private fun fillChoreData() {
        val choreModel = choreModel!!

        if (choreModel.isTimed) {
            val lastDue = Calendar.getInstance()
            lastDue.timeInMillis = choreModel.whenDue
            val id = RepeatInterval.values().indexOf(choreModel.repeatsEvery)

            val timeFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
            val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

            val formattedTime = timeFormat.format(lastDue.time)
            val formattedDate = dateFormat.format(lastDue.time)

            binding.choreDetailDueDate.text = formattedDate
            binding.choreDetailDueTime.text = formattedTime
            binding.choreIntervalSpinner.setSelection(id)
            binding.choreDetailIsTimedBox.isChecked = true

            toggleTimeUI(VISIBLE)
        } else {
            toggleTimeUI(GONE)
        }

        // Fill in existing data
        binding.createChoreTitle.setText(R.string.chore_detail_title_edit)
        binding.createChoreNameInput.editText!!.setText(choreModel.choreName)
        binding.choreDetailAssignedTo.text = choreModel.assignedTo.joinToString()
        binding.choreDetailMinsToComplete.setText(choreModel.timeToComplete.toString())
        binding.choreDetailPoints.text = choreModel.points.toString()
    }

    companion object {
        const val TAG = "CreateChoreDialog"
        const val TIME_PICKER = 1

        fun newInstance(choreModel: ChoreModel?,
                        state: DialogState): ChoreDetailDialog{
            val fragment = ChoreDetailDialog()
            val args = Bundle()
            choreModel?.let {ChoreUtil.addChoreToArgs(args, it)}
            val id = DialogState.values().indexOf(state)
            args.putInt("dialogState", id)
            fragment.arguments = args
            return fragment
        }
    }
}


