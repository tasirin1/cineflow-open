package com.cineflow.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler

/**
 * Download Service — mengikuti pola CineFlow asli.
 * Extends Media3's DownloadService untuk background downloads.
 */
class MyDownloadService : DownloadService(
    NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    0
) {
    companion object {
        const val NOTIFICATION_ID = 40
        const val CHANNEL_ID = "cineflow_download_channel"
    }

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = DownloadManager(
            this,
            getDatabaseProvider(),
            getCache(),
            getUpstreamDataSourceFactory(),
            getDownloadExecutor()
        )
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                // Handle download state changes
            }
        })
        return downloadManager
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Unduhan",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("CineFlow")
            .setContentText("Mengunduh...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }
}
