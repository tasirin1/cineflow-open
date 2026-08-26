package com.cineflow.app.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.cineflow.app.CineFlowApp
import com.cineflow.app.data.DownloadState
import com.cineflow.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastUiUpdate = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        runCatching { startForegroundCompat() }

        scope.launch {
            CineFlowApp.engine.items.collect { items ->
                val active = items.any { it.isActive }
                updateWakeLock(active)
                if (!active) {
                    NotificationHelper.updateNotification(this@DownloadService, items)
                    ServiceCompat.stopForeground(
                        this@DownloadService,
                        ServiceCompat.STOP_FOREGROUND_REMOVE
                    )
                    stopSelf()
                } else {
                    val now = System.currentTimeMillis()
                    if (now - lastUiUpdate < 1000) return@collect
                    lastUiUpdate = now
                    NotificationHelper.updateNotification(this@DownloadService, items)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationHelper.ACTION_PAUSE_ALL -> CineFlowApp.engine.pauseAll()
            NotificationHelper.ACTION_RESUME_ALL -> CineFlowApp.engine.resumeAll()
        }
        return START_STICKY
    }

    private fun startForegroundCompat() {
        val notification = NotificationHelper.foregroundNotification(this)
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        runCatching { wakeLock?.release() }
        wakeLock = null
        scope.cancel()
        super.onDestroy()
    }

    private fun updateWakeLock(active: Boolean) {
        if (active) {
            if (wakeLock == null) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cineflow:download")
                    .apply { setReferenceCounted(false) }
            }
            val lock = wakeLock
            if (lock != null && !lock.isHeld) {
                runCatching { lock.acquire(15 * 60 * 1000L) }
            }
        } else {
            runCatching { wakeLock?.release() }
            wakeLock = null
        }
    }
}
