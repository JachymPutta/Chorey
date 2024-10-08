package com.chorey.util

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.chorey.R
import com.chorey.RANDOM_SEED
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import java.util.UUID
import kotlin.random.Random

object HomeUtil {
    private var seed = RANDOM_SEED

    fun makeRandomHome(context: Context) : HomeModel {
        val random = Random(seed)
        var homeNames = context.resources.getStringArray(R.array.home_names)

        homeNames = homeNames.copyOfRange(1, homeNames.size)
        seed = random.nextInt()

        return HomeModel(
            homeUID = UUID.randomUUID().toString(),
            homeName = getRandomString(homeNames, random)
        )
    }

    fun getRandomUsers() : ArrayList<HomeUserModel> {
        val user1 = HomeUserModel(
            name = "John",
            icon = R.drawable.user_icon1
        )

        val user2 = HomeUserModel(
            name = "Thomas",
            icon = R.drawable.user_icon2
        )

        val user3 = HomeUserModel(
            name = "Andrew",
            icon = R.drawable.user_icon3
        )

        val user4 = HomeUserModel(
            name = "Bob",
            icon = R.drawable.user_icon4
        )

        return arrayListOf(user1, user2, user3, user4)
    }

//    fun getHomeFromArgs(arguments : Bundle) : HomeModel {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arguments.getParcelable(HomeModel.toString(), HomeModel::class.java)!!
//        } else {
//            val uid = arguments.getString(HomeModel.FIELD_UID)!!
//            val name = arguments.getString(HomeModel.FIELD_NAME)!!
//            val icon = arguments.getInt(HomeModel.FIELD_ICON)
//            val users = arguments.getStringArrayList(HomeModel.FIELD_USERS)!!
//            HomeModel(uid, name, icon, users)
//        }
//    }

//    fun addHomeToArgs(args : Bundle, home: HomeModel) : Bundle {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            args.apply { putParcelable(HomeModel.toString(), home) }
//        } else {
//            args.apply {
//                putString(HomeModel.FIELD_UID, home.homeUID)
//                putString(HomeModel.FIELD_NAME, home.homeName)
//                putInt(HomeModel.FIELD_ICON, home.icon)
//                putStringArrayList(HomeModel.FIELD_USERS, home.users)
//            }
//        }
//    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }


}