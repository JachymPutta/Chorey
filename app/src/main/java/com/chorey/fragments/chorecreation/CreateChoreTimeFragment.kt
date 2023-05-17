package com.chorey.fragments.chorecreation

import android.app.TimePickerDialog
import android.content.res.Resources.Theme
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chorey.Chorey
import com.chorey.R
import com.chorey.TIME_PATTERN
import com.chorey.data.ChoreModel
import com.chorey.data.HomeModel
import com.chorey.data.RepeatInterval
import com.chorey.databinding.FragmentCreateChoreTimeBinding
import com.chorey.dialog.ChoreDetailDialog
import com.chorey.util.ChoreUtil
import com.chorey.viewmodel.ChoreViewModel
import com.chorey.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateChoreTimeFragment : Fragment() {

    private lateinit var binding: FragmentCreateChoreTimeBinding

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val choreViewModel by activityViewModels<ChoreViewModel>()

    private lateinit var home : HomeModel
    private lateinit var chore : ChoreModel

    private var curInterval = RepeatInterval.None
    private var curDate = Calendar.getInstance()

    private lateinit var allButtons : ArrayList<TextView>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        home = homeViewModel.home.value!!
        chore = choreViewModel.chore.value!!
        binding = FragmentCreateChoreTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allButtons = arrayListOf(
            binding.repeatNo, binding.repeatAsNeeded, binding.repeatDaily,
            binding.repeatWeekly, binding.repeatMonthly, binding.repeatAnnually)

        curDate.timeInMillis =  binding.dateCalendar.date

        binding.completeButton.setOnClickListener { onCompleteHandle() }
        binding.cancelButton.setOnClickListener { returnToHome() }
        binding.scheduleText.setOnClickListener {onSelectTime()}
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.dateCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            curDate.set(year, month, dayOfMonth)
        }

        repeatSelectionHandle()
    }

    private fun onSelectTime() {
        val currentTime = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                curDate.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                val timeFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
                binding.scheduleText.text = timeFormat.format(curDate.time)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            false
        )

        // Show the TimePickerDialog
        timePickerDialog.show()

    }

    private fun onCompleteHandle() {
        chore.apply {
            //TODO: update timing data
            isTimed = true
            whenDue = curDate.timeInMillis
            timeToComplete = 1
            points = ChoreUtil.getPoints(1)
            repeatsEvery = curInterval
        }

        choreViewModel.updateChore(chore)
        choreViewModel.addChoreToHome()

        returnToHome()
    }

    private fun repeatSelectionHandle() {
        allButtons.forEach { b -> b.setOnClickListener {
            highlightButton(b)
            when (b) {
                binding.repeatNo -> curInterval = RepeatInterval.None
                binding.repeatAsNeeded -> curInterval = RepeatInterval.AsNeeded
                binding.repeatDaily -> curInterval = RepeatInterval.Day
                binding.repeatWeekly -> curInterval = RepeatInterval.Week
                binding.repeatMonthly -> curInterval = RepeatInterval.Month
                binding.repeatAnnually -> curInterval = RepeatInterval.Year
            }
        }
        }
    }
    private fun highlightButton(button: TextView) {

        allButtons.forEach {curButton ->
            if (curButton == button) {
                // Highlight
                curButton.setBackgroundColor(requireContext().getColor(R.color.secondary_color))
            } else {
                // Remove highlight
                curButton.setBackgroundColor(requireContext().getColor(R.color.primary_color))
            }
        }
    }

    private fun returnToHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }

}