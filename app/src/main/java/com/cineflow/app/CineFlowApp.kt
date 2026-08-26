package com.cineflow.app

import android.app.Application
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.cineflow.app.download.DownloadEngine

class CineFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        engine = DownloadEngine(this)
        Thread { runCatching { engine.cleanupOrphans() } }.start()
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        runCatching {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runCatching { engine.resumeAutoPaused() }
                }
            }
            if (Build.VERSION.SDK_INT >= 24) {
                cm.registerDefaultNetworkCallback(callback)
            } else {
                cm.registerNetworkCallback(request, callback)
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var engine: DownloadEngine
            private set
    }
}
