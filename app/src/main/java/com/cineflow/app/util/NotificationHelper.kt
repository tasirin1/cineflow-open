package com.cineflow.app.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cineflow.app.CineFlowApp
import com.cineflow.app.MainActivity
import com.cineflow.app.R
import com.cineflow.app.data.DownloadItem
import com.cineflow.app.data.DownloadState
import com.cineflow.app.download.DownloadService

object NotificationHelper {

    const val NOTIFICATION_ID = 40
    private const val CHANNEL_ID = "cineflow_download_channel"
    const val ACTION_PAUSE_ALL = "com.cineflow.app.ACTION_PAUSE_ALL"
    const val ACTION_RESUME_ALL = "com.cineflow.app.ACTION_RESUME_ALL"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Unduhan",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi untuk unduhan video"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun foregroundNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("CineFlow")
            .setContentText("Mengelola unduhan...")
            .setOngoing(true)
            .build()
    }

    fun updateNotification(context: Context, items: List<DownloadItem>) {
        val downloading = items.count { it.state == DownloadState.DOWNLOADING }
        val completed = items.count { it.state == DownloadState.COMPLETED }
        val failed = items.count { it.state == DownloadState.FAILED }

        val title = when {
            downloading > 0 -> "Mengunduh $downloading item..."
            completed > 0 -> "$completed unduhan selesai"
            else -> "CineFlow"
        }

        val text = buildString {
            if (downloading > 0) append("Mengunduh")
            if (completed > 0) { if (isNotEmpty()) append(" • "); append("$completed selesai") }
            if (failed > 0) { if (isNotEmpty()) append(" • "); append("$failed gagal") }
            if (isEmpty()) append("Tidak ada unduhan aktif")
        }

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            context, 1,
            Intent(context, DownloadService::class.java).apply { action = ACTION_PAUSE_ALL },
            PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = PendingIntent.getService(
            context, 2,
            Intent(context, DownloadService::class.java).apply { action = ACTION_RESUME_ALL },
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setOngoing(downloading > 0)

        if (downloading > 0) {
            builder.addAction(R.drawable.ic_launcher_foreground, "Jeda", pauseIntent)
        } else if (items.any { it.state == DownloadState.PAUSED }) {
            builder.addAction(R.drawable.ic_launcher_foreground, "Lanjutkan", resumeIntent)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, builder.build())
    }
}
