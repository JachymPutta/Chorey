package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.DATE_PATTERN
import com.chorey.TIME_PATTERN
import com.chorey.data.ChoreModel
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.DialogHistoryDetailBinding
import com.chorey.util.ChoreUtil
import com.chorey.util.HomeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryDetailDialog : DialogFragment() {
    private var _binding: DialogHistoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var choreModel: ChoreModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        choreModel = ChoreUtil.getChoreFromArgs(requireArguments())
        _binding = DialogHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reward =  choreModel.points.toString() + " points"

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.createChoreName.text = choreModel.choreName
        binding.choreDetailDoneBy.text = choreModel.curAssignee
        binding.choreDetailMinsToComplete.text = choreModel.timeToComplete.toString()
        binding.choreDetailPoints.text = reward

        val lastDue = Calendar.getInstance()
        lastDue.timeInMillis = choreModel.whenDue

        val timeFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

        val formattedTime = timeFormat.format(lastDue.time)
        val formattedDate = dateFormat.format(lastDue.time)

        binding.choreDetailDueDate.text = formattedDate
        binding.choreDetailDueTime.text = formattedTime
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "HistoryDialog"
        fun newInstance(choreModel: ChoreModel): HistoryDetailDialog{
            val fragment = HistoryDetailDialog()
            val args = ChoreUtil.addChoreToArgs(Bundle(), choreModel)
            fragment.arguments = args
            return fragment
        }
    }
}