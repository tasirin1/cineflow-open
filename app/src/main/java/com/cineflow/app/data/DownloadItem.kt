package com.cineflow.app.data

enum class DownloadState {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED;

    val isFinished: Boolean get() = this == COMPLETED || this == FAILED || this == CANCELLED
}

data class DownloadItem(
    val id: String,
    val url: String,
    val title: String = "",
    val episode: String = "",
    val quality: String = "",
    val state: DownloadState,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null,
    val contentUri: String? = null,
    val filePath: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long = 0,
    val posterUrl: String = "",
    val bookId: String = "",
    val source: String = "",
    val chapterNum: Int = 0,
    val description: String = "",
    val episodeId: String? = null,
    val episodeThumbnailUrl: String? = null,
    val initialVideoUrl: String? = null,
    val totalChapters: Int = 0,
    val preferredHeight: Int = 0,
    val headers: Map<String, String>? = null,
    val subtitlesJson: String? = null,
    val streamKeysJson: String? = null,
    val audioUrl: String? = null,
    val isAudioPart: Boolean = false,
    val drmScheme: String? = null,
    val drmLicenseUrl: String? = null,
    val drmBase64Key: String? = null,
    val speedBps: Long = 0,
    val etaSeconds: Long = 0
) {
    val progressPercent: Int
        get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0

    val isPending: Boolean get() = id.startsWith("pending_")
    val isActive: Boolean get() = state == DownloadState.DOWNLOADING || state == DownloadState.PENDING
}
