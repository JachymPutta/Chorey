package com.chorey.util

import android.content.Context
import android.util.Log
import com.chorey.BuildConfig
import com.chorey.POINTS_MULTIPLIER
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.ChoreModel
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

    fun getPoints(timeToComplete : Int) = timeToComplete * POINTS_MULTIPLIER

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }
}