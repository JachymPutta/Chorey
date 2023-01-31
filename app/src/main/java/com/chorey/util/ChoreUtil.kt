package com.chorey.util

import android.content.Context
import android.util.Log
import com.chorey.BuildConfig
import com.chorey.POINTS_MULTIPLIER
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.ChoreModel
import com.chorey.data.RepeatInterval
import com.chorey.data.UserModel
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

    fun getPoints(hrs : Int, min : Int) : Int {
        return ((hrs * 60) + min) * POINTS_MULTIPLIER
    }

    fun updateData(oldChore:ChoreModel, completedBy: UserModel) : ChoreModel {

        val newChore = updateTime(oldChore, ChoreModel())
        updateAssignment(newChore, completedBy)



        return newChore
    }

    private fun updateTime(oldChore:ChoreModel, newChore: ChoreModel) : ChoreModel {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = oldChore.whenDue!!
        when (oldChore.repeatsEvery) {
            RepeatInterval.None -> {}
            RepeatInterval.Year -> calendar.add(Calendar.YEAR, 1)
            RepeatInterval.Month -> calendar.add(Calendar.MONTH, 1)
            RepeatInterval.Day -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RepeatInterval.Hour -> calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        newChore.whenDue = calendar.timeInMillis
        return newChore
    }

    private fun updateAssignment(choreModel: ChoreModel, completedBy: UserModel) {
        // TODO: needs separate assignment field to change it

    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }
}