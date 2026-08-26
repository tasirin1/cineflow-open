package com.cineflow.app

import android.app.Application
import android.util.Log
import com.cineflow.app.data.api.SessionClient
import com.cineflow.app.data.api.SessionManager

class VideoStreamingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionClient.init(this)
        SessionManager.init(this)
        Log.d("CineFlow", "VideoStreamingApp initialized")
    }
}
