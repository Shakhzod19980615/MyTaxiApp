package com.example.mytaxiapp

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this,
            "pk.eyJ1Ijoic2hha2h6b2RiZWsxOTk4IiwiYSI6ImNsemQ4cWxrazBpMngya3FxeXVoaGI1ZTEifQ.kGdOBXEzaQgz35yP9bDGHw")
    }
}