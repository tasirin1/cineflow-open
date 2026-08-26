package com.cineflow.app.download

import android.content.Context
import android.util.Log
import com.cineflow.app.data.DownloadItem
import com.cineflow.app.data.DownloadItemCodec
import com.cineflow.app.data.DownloadRepository
import com.cineflow.app.data.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Inti unduhan — model Tasirin DownloadManager.
 *
 * Engine hidup seumur proses (disimpan statis di App.engine).
 * Menyimpan Application context saja, jangan Activity (anti-leak).
 */
class DownloadEngine(appContext: Context) {

    private val context: Context = appContext.applicationContext
    private val repository = DownloadRepository(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Job>()
    private val saveJob = AtomicBoolean(false)
    private var progressSaveJob: Job? = null

    private val _items = MutableStateFlow<List<DownloadItem>>(repository.load())
    val items: StateFlow<List<DownloadItem>> = _items.asStateFlow()

    init {
        // Auto-save progress tiap 2 detik saat ada download aktif
        scope.launch {
            while (true) {
                delay(2000)
                val active = _items.value.filter { it.isActive }
                if (active.isNotEmpty()) {
                    repository.saveProgress(active)
                }
            }
        }
    }

    fun addItem(item: DownloadItem) {
        val current = _items.value.toMutableList()
        current.add(item)
        _items.value = current
        saveSnapshot()
        startDownload(item)
    }

    fun pauseItem(id: String) {
        updateItemState(id, DownloadState.PAUSED)
        jobs[id]?.cancel()
        jobs.remove(id)
    }

    fun resumeItem(id: String) {
        val item = _items.value.find { it.id == id } ?: return
        if (item.state == DownloadState.PAUSED || item.state == DownloadState.FAILED) {
            updateItemState(id, DownloadState.PENDING)
            startDownload(item.copy(state = DownloadState.PENDING))
        }
    }

    fun removeItem(id: String) {
        jobs[id]?.cancel()
        jobs.remove(id)
        val current = _items.value.toMutableList()
        current.removeAll { it.id == id }
        _items.value = current
        saveSnapshot()
    }

    fun pauseAll() {
        _items.value.filter { it.isActive }.forEach { pauseItem(it.id) }
    }

    fun resumeAll() {
        _items.value.filter { it.state == DownloadState.PAUSED }.forEach { resumeItem(it.id) }
    }

    fun resumeInterrupted() {
        _items.value.filter {
            it.state == DownloadState.DOWNLOADING || it.state == DownloadState.PENDING
        }.forEach {
            updateItemState(it.id, DownloadState.PAUSED)
        }
        resumeAll()
    }

    fun resumeAutoPaused() {
        _items.value.filter { it.state == DownloadState.PAUSED }.forEach { resumeItem(it.id) }
    }

    fun cleanupOrphans() {
        // Hapus file partial yang tidak memiliki item aktif
        // TODO: Implement file cleanup logic
    }

    private fun startDownload(item: DownloadItem) {
        if (jobs.containsKey(item.id)) return

        val job = scope.launch {
            try {
                updateItemState(item.id, DownloadState.DOWNLOADING)
                // TODO: Implement actual HTTP download with range support
                // For now, simulate download progress
                simulateDownload(item)
            } catch (e: Exception) {
                Log.e(TAG, "Download failed: ${item.id}", e)
                updateItemState(item.id, DownloadState.FAILED, error = e.message)
            }
        }
        jobs[item.id] = job
    }

    private suspend fun simulateDownload(item: DownloadItem) {
        // Placeholder: simulate download progress
        val totalBytes = 1024L * 1024L * 10 // 10MB
        var downloaded = 0L
        while (downloaded < totalBytes) {
            delay(100)
            downloaded += 1024L * 100 // 100KB per tick
            val current = _items.value.map {
                if (it.id == item.id) it.copy(
                    bytesDownloaded = downloaded,
                    totalBytes = totalBytes
                ) else it
            }
            _items.value = current
        }
        updateItemState(item.id, DownloadState.COMPLETED)
    }

    private fun updateItemState(id: String, state: DownloadState, error: String? = null) {
        val current = _items.value.map {
            if (it.id == id) it.copy(
                state = state,
                error = error,
                finishedAt = if (state.isFinished) System.currentTimeMillis() else 0
            ) else it
        }
        _items.value = current
        saveSnapshot()
    }

    private fun saveSnapshot() {
        if (saveJob.compareAndSet(false, true)) {
            scope.launch {
                repository.save(_items.value)
                saveJob.set(false)
            }
        }
    }

    companion object {
        private const val TAG = "DownloadEngine"
    }
}
