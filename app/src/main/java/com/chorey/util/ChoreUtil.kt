package com.chorey.util

import android.content.Context
import com.chorey.MINS_MULTIPLIER
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.ChoreModel
import com.chorey.data.RepeatInterval
import com.chorey.data.LoggedUserModel
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

object ChoreUtil {
    private const val TAG = "ChoreUtil"
    private var seed = RANDOM_SEED

    fun makeRandomChore(context: Context) : ChoreModel {
        val random = Random(seed)
        var choreNames = context.resources.getStringArray(R.array.chore_names)
        choreNames = choreNames.copyOfRange(1, choreNames.size)
        seed = random.nextInt()

        return ChoreModel(
            UID = UUID.randomUUID().toString(),
            choreName = getRandomString(choreNames, random),
            homeId = "home_name"
        )
    }

    fun getPoints(min : Int) : Int {
        return min * MINS_MULTIPLIER
    }

    fun completeChore(chore: ChoreModel, completedBy: LoggedUserModel) {
        chore.isTimed = true
        chore.whenDue = Calendar.getInstance().timeInMillis
        chore.repeatsEvery = RepeatInterval.None
        chore.curAssignee = completedBy.name
    }

    fun updateData(chore:ChoreModel, completedBy: LoggedUserModel) : ChoreModel {

        chore.UID = UUID.randomUUID().toString()

        updateTime(chore)
        updateAssignment(chore, completedBy)

        return chore
    }

    private fun updateTime(oldChore:ChoreModel) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = oldChore.whenDue
        when (oldChore.repeatsEvery) {
            RepeatInterval.None -> {}
            RepeatInterval.Year -> calendar.add(Calendar.YEAR, 1)
            RepeatInterval.Month -> calendar.add(Calendar.MONTH, 1)
            RepeatInterval.Week -> calendar.add(Calendar.DAY_OF_MONTH, 7)
            RepeatInterval.Day -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RepeatInterval.Hour -> calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        oldChore.whenDue = calendar.timeInMillis
    }

    private fun updateAssignment(chore : ChoreModel, completedBy: LoggedUserModel) {
        if (chore.curAssignee == completedBy.name) {
            val oldId = chore.assignedTo.indexOf(chore.curAssignee)
            // Increment by 1 - loop around if end of array
            val newId = (oldId + 1) % chore.assignedTo.size

            val newAssignee = chore.assignedTo[newId]
            chore.curAssignee = newAssignee
            chore.assignedTo = chore.assignedTo
        } else {
            chore.curAssignee = chore.curAssignee
        }
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }
}