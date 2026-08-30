package com.cineflow.app

import android.app.Application
import android.content.Context
import android.os.Build
import com.cineflow.app.data.api.SessionManager
import com.cineflow.app.util.AppLogger

class VideoStreamingApp : Application() {

    companion object {
        private const val TAG = "VideoStreamingApp"
        var instance: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = applicationContext

        AppLogger.d(TAG, "=== CINEFLOW OPEN BOOT ===")
        AppLogger.d(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        AppLogger.d(TAG, "App version: ${packageManager.getPackageInfo(packageName, 0).versionName} (code ${packageManager.getPackageInfo(packageName, 0).versionCode})")
        AppLogger.d(TAG, "Package: $packageName")

        SessionManager.init(this)
        AppLogger.d(TAG, "SessionManager.init selesai")
    }
}
