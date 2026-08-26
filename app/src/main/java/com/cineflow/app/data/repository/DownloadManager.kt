package com.cineflow.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cineflow.app.data.model.DrmInfoData
import com.cineflow.app.data.model.DownloadItem
import com.cineflow.app.data.model.DownloadMetadata
import com.cineflow.app.data.model.UnifiedDetailResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Download Manager — mengikuti pola CineFlow asli (h6.r).
 *
 * Singleton yang mengelola seluruh unduhan dengan:
 * - ConcurrentHashMap untuk active downloads (f5729i)
 * - ConcurrentHashMap untuk pending queue (f5730j)
 * - StateFlow untuk reactive UI (f5723b)
 * - JSON file persistence via Gson
 */
class DownloadManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "DownloadManager"

        @Volatile
        private var instance: DownloadManager? = null

        fun getInstance(context: Context): DownloadManager {
            return instance ?: synchronized(this) {
                instance ?: DownloadManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val gson = Gson()
    private val jsonType = object : TypeToken<List<DownloadMetadata>>() {}.type
    private val downloadListType = object : TypeToken<List<DownloadItem>>() {}.type

    // Active downloads (f5729i)
    private val activeDownloads = ConcurrentHashMap<String, DownloadItem>()

    // Pending downloads queue (f5730j)
    private val pendingDownloads = ConcurrentHashMap<String, DownloadItem>()

    // Deleted IDs tracking (f5731k)
    private val deletedIds = mutableSetOf<String>()

    // Download list observable (f5723b)
    private val _downloadItems = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadItems: StateFlow<List<DownloadItem>> = _downloadItems.asStateFlow()

    init {
        loadPendingDownloads()
    }

    /**
     * Get all download items (f5722a.B() pattern)
     */
    fun getAllDownloads(): List<DownloadItem> {
        return _downloadItems.value
    }

    /**
     * Get download item by ID (f5722a.A() pattern)
     */
    fun getDownloadById(id: String): DownloadItem? {
        activeDownloads[id]?.let { return it }
        return _downloadItems.value.find { it.id == id }
    }

    /**
     * Add a pending download (when ExoManager isn't ready)
     */
    fun addPendingDownload(item: DownloadItem): DownloadItem {
        val pendingId = "pending_${item.bookId}_${item.episodeId ?: item.chapterNum}"
        val pendingItem = item.copy(
            id = pendingId,
            status = "Antrean",
            progress = 0
        )
        pendingDownloads[pendingId] = pendingItem
        savePendingDownloads()
        refreshDownloadList()
        return pendingItem
    }

    /**
     * Update download status (f5722a.a0() pattern)
     */
    fun updateDownloadStatus(id: String, status: String, progress: Int = 0) {
        val item = activeDownloads[id] ?: pendingDownloads[id] ?: return
        val updatedItem = item.copy(status = status, progress = progress)

        if (pendingDownloads.containsKey(id)) {
            pendingDownloads[id] = updatedItem
        } else {
            activeDownloads[id] = updatedItem
        }
        refreshDownloadList()
    }

    /**
     * Delete a download (f5722a.w() pattern)
     */
    fun deleteDownload(id: String) {
        deletedIds.add(id)

        if (id.startsWith("pending_")) {
            pendingDownloads.remove(id)
            savePendingDownloads()
        } else {
            activeDownloads.remove(id)
        }
        refreshDownloadList()
    }

    /**
     * Pause all downloads for a series (f5722a.L() pattern)
     */
    fun pauseSeriesDownloads(bookId: String) {
        getDownloadsForSeries(bookId)
            .filter { it.status != "Selesai" }
            .forEach { updateDownloadStatus(it.id, "Dijeda") }
    }

    /**
     * Resume all downloads (f5722a.R() pattern)
     */
    fun resumeAllDownloads() {
        getAllDownloads()
            .filter { it.status != "Selesai" }
            .forEach { updateDownloadStatus(it.id, "Menunggu...") }
    }

    /**
     * Get downloads for a specific series
     */
    fun getDownloadsForSeries(bookId: String): List<DownloadItem> {
        return getAllDownloads().filter { it.bookId == bookId }
    }

    /**
     * Check if an episode has been downloaded
     */
    fun isEpisodeDownloaded(episodeId: String): Boolean {
        return getAllDownloads().any {
            it.episodeId == episodeId && it.status == "Selesai"
        }
    }

    /**
     * Save metadata for a series (f5722a.U() pattern)
     */
    fun saveSeriesMetadata(bookId: String, detail: UnifiedDetailResponse) {
        try {
            val file = getMetadataFile(bookId)
            file.writeText(gson.toJson(detail))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save metadata for $bookId", e)
        }
    }

    /**
     * Read cached detail (f5722a.C() pattern)
     */
    fun readCachedDetail(bookId: String): UnifiedDetailResponse? {
        return try {
            val file = getMetadataFile(bookId)
            if (file.exists()) {
                gson.fromJson(file.readText(), UnifiedDetailResponse::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read cached detail for $bookId", e)
            null
        }
    }

    // ==================== Private Helpers ====================

    private fun refreshDownloadList() {
        val allItems = mutableListOf<DownloadItem>()
        allItems.addAll(activeDownloads.values)
        allItems.addAll(pendingDownloads.values)
        _downloadItems.value = allItems
    }

    private fun savePendingDownloads() {
        try {
            val file = File(context.filesDir, "pending_downloads.json")
            if (pendingDownloads.isEmpty()) {
                if (file.exists()) file.delete()
                return
            }
            file.writeText(gson.toJson(pendingDownloads.values.toList()))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save pending downloads", e)
        }
    }

    private fun loadPendingDownloads() {
        try {
            val file = File(context.filesDir, "pending_downloads.json")
            if (!file.exists()) return
            val items: List<DownloadMetadata> = gson.fromJson(file.readText(), jsonType)
            items.forEach { meta ->
                val item = DownloadItem(
                    id = "pending_${meta.bookId}_${meta.episodeId ?: meta.chapterNum}",
                    title = meta.title,
                    episode = meta.episode,
                    quality = meta.quality,
                    status = "Antrean",
                    posterUrl = meta.posterUrl,
                    bookId = meta.bookId,
                    source = meta.source,
                    chapterNum = meta.chapterNum,
                    description = meta.description ?: "",
                    subtitlePath = meta.subtitlePath,
                    subtitlesJson = meta.subtitlesJson,
                    localPosterPath = meta.localPosterPath,
                    localEpisodeThumbnailPath = meta.localEpisodeThumbnailPath,
                    totalChapters = meta.totalChapters,
                    preferredHeight = meta.preferredHeight,
                    headers = meta.headers,
                    episodeThumbnailUrl = meta.episodeThumbnailUrl,
                    initialVideoUrl = meta.initialVideoUrl,
                    episodeId = meta.episodeId,
                    streamKeysJson = meta.streamKeysJson,
                    audioUrl = meta.audioUrl,
                    isAudioPart = meta.isAudioPart,
                    timestamp = meta.timestamp,
                    drm = meta.drm?.let {
                        DrmInfoData(it.scheme, it.licenseUrl, it.headers, it.base64Key)
                    }
                )
                pendingDownloads[item.id] = item
            }
            refreshDownloadList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load pending downloads", e)
        }
    }

    private fun getMetadataFile(bookId: String): File {
        val sanitized = bookId.lowercase().replace(Regex("[^a-z0-9._-]"), "_")
            .trimEnd('_').ifBlank { "item" }
        val hash = sha1(bookId).take(16)
        return File(context.filesDir, "meta_${sanitized}_${hash}.json")
    }

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        return md.digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
