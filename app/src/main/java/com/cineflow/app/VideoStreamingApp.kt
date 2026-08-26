package com.cineflow.app

import android.app.Application
import com.cineflow.app.data.repository.DownloadManager

class VideoStreamingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize download manager singleton
        DownloadManager.getInstance(this)
    }
}
