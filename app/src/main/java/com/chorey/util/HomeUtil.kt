package com.chorey.util

import android.content.Context
import android.util.Log
import android.widget.EditText
import com.chorey.BuildConfig
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.HomeModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlin.random.Random

object HomeUtil {
    private const val TAG = "HomeUtil"
    private var seed = RANDOM_SEED

    fun makeRandomHome(context: Context) : HomeModel {
        val home = HomeModel()
        val random = Random(seed)

        if (BuildConfig.DEBUG) {
            var homeNames = context.resources.getStringArray(R.array.home_names)
            homeNames = homeNames.copyOfRange(1, homeNames.size)
            home.homeName = getRandomString(homeNames, random)
        } else {
            Log.d(TAG, "WARNING: Using random home outside debugging!")
        }

        //Regenerate randomness
        seed = random.nextInt()
        return home
    }

    fun getHomeFromSnap(snap : DocumentSnapshot) : HomeModel?{
        var home: HomeModel? = null

        snap.reference.get()
            .addOnSuccessListener { doc ->
                home = doc.toObject<HomeModel>()
            }.addOnFailureListener{
                e -> Log.d(TAG, "Error fetching home from snap: $e!")
            }
        return home
    }

    fun isEmpty(editText: EditText) : Boolean = editText.text.toString().isNotBlank()

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }


}