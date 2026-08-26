package com.cineflow.app

import android.app.Application
import android.util.Log

class VideoStreamingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("CineFlow", "VideoStreamingApp initialized")
    }
}
