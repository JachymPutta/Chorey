package com.chorey.util

import android.content.Context
import android.util.Log
import com.chorey.BuildConfig
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.ChoreModel
import kotlin.random.Random

object ChoreUtil {
    private const val TAG = "ChoreUtil"

    fun makeRandomChore(context: Context) : ChoreModel {
        val chore = ChoreModel()
        val random = Random(RANDOM_SEED)

        if (BuildConfig.DEBUG) {
            var choreNames = context.resources.getStringArray(R.array.chore_names)
            choreNames = choreNames.copyOfRange(1, choreNames.size)
            chore.choreName = getRandomString(choreNames, random)
        } else {
            Log.d(TAG, "WARNING: Using random chore outside debugging!")
        }

        return chore
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }
}