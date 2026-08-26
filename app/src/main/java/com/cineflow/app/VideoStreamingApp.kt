package com.cineflow.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.cineflow.app.data.api.SessionManager

class VideoStreamingApp : Application() {

    companion object {
        var instance: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = applicationContext
        SessionManager.init(this)
        Log.d("CineFlow", "VideoStreamingApp initialized")
    }
}
