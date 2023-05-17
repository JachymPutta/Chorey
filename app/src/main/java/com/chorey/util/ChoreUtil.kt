package com.chorey.util

import android.content.Context
import android.os.Build
import android.os.Bundle
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
    private var seed = RANDOM_SEED

    fun makeRandomChore(context: Context) : ChoreModel {
        val random = Random(seed)
        var choreNames = context.resources.getStringArray(R.array.chore_names)
        choreNames = choreNames.copyOfRange(1, choreNames.size)
        seed = random.nextInt()

        return ChoreModel(
            choreUID = UUID.randomUUID().toString(),
            choreName = getRandomString(choreNames, random),
            homeId = "home_name"
        )
    }

    fun getChoreFromArgs(arguments : Bundle) : ChoreModel {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments.getParcelable(ChoreModel.toString(), ChoreModel::class.java)!!
        } else {
            val uid = arguments.getString(ChoreModel.FIELD_UID)!!
            val name = arguments.getString(ChoreModel.FIELD_NAME)!!
            val desc = arguments.getString(ChoreModel.FIELD_DESCRIPTION)!!
            val homeId = arguments.getString(ChoreModel.FIELD_HOME_ID)!!
            val timed = arguments.getBoolean(ChoreModel.FIELD_IS_TIMED)
            val whenDue = arguments.getLong(ChoreModel.FIELD_WHEN_DUE)
            val timeToDo = arguments.getInt(ChoreModel.FIELD_TIME_TO_COMPLETE)
            val allAssign = arguments.getStringArrayList(ChoreModel.FIELD_ASSIGNED_TO)!!
            val curAssign = arguments.getString(ChoreModel.FIELD_CUR_ASSIGNEE)!!
            val points = arguments.getInt(ChoreModel.FIELD_POINTS)
            val repeatId = arguments.getInt(ChoreModel.FIELD_REPEATS_EVERY)

            val repeat = RepeatInterval.values()[repeatId]
            ChoreModel(uid, name, desc, homeId, timed,
                whenDue, timeToDo, allAssign, curAssign, points, repeat)
        }
    }

    fun addChoreToArgs(args : Bundle, chore: ChoreModel) : Bundle {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.apply { putParcelable(ChoreModel.toString(), chore) }
        } else {
            args.apply {
                putString(ChoreModel.FIELD_UID, chore.choreUID)
                putString(ChoreModel.FIELD_NAME, chore.choreName)
                putString(ChoreModel.FIELD_DESCRIPTION, chore.choreDescription)
                putString(ChoreModel.FIELD_HOME_ID, chore.homeId)
                putBoolean(ChoreModel.FIELD_IS_TIMED, chore.isTimed)
                putLong(ChoreModel.FIELD_WHEN_DUE, chore.whenDue)
                putInt(ChoreModel.FIELD_TIME_TO_COMPLETE, chore.timeToComplete)
                putStringArrayList(ChoreModel.FIELD_ASSIGNED_TO, chore.assignedTo)
                putString(ChoreModel.FIELD_CUR_ASSIGNEE, chore.curAssignee)
                putInt(ChoreModel.FIELD_POINTS, chore.points)
                val id = RepeatInterval.values().indexOf(chore.repeatsEvery)
                putInt(ChoreModel.FIELD_REPEATS_EVERY, id)
            }
        }
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

        chore.choreUID = UUID.randomUUID().toString()

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