package com.chorey

import android.app.Application

class Chorey : Application() {
    var initMenuLoad = false

    override fun onCreate() {
        super.onCreate()
        initMenuLoad = true
    }
}