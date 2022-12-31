package com.chorey.util

import android.content.Context
import android.util.Log
import com.chorey.BuildConfig
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.HomeModel
import kotlin.random.Random

object HomeUtil {
    private const val TAG = "HomeUtil"

    fun makeRandomHome(context: Context) : HomeModel {
        val home = HomeModel()
        val random = Random(RANDOM_SEED)

        if (BuildConfig.DEBUG) {
            var homeNames = context.resources.getStringArray(R.array.home_names)
            homeNames = homeNames.copyOfRange(1, homeNames.size)
            home.homeName = getRandomString(homeNames, random)
        } else {
            Log.d(TAG, "WARNING: Using random home outside debugging!")
        }

        return home
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

}