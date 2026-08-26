package com.cineflow.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialisasi daftar unduhan ke/dari JSON — murni JVM (tanpa Android),
 * supaya roundtrip bisa diuji unit di CI.
 */
object DownloadItemCodec {

    fun encode(items: List<DownloadItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val o = JSONObject()
            o.put("id", item.id)
            o.put("url", item.url)
            o.put("title", item.title)
            o.put("episode", item.episode)
            o.put("quality", item.quality)
            o.put("state", item.state.name)
            o.put("bytesDownloaded", item.bytesDownloaded)
            o.put("totalBytes", item.totalBytes)
            item.error?.let { o.put("error", it) }
            item.contentUri?.let { o.put("contentUri", it) }
            item.filePath?.let { o.put("filePath", it) }
            o.put("addedAt", item.addedAt)
            o.put("finishedAt", item.finishedAt)
            o.put("posterUrl", item.posterUrl)
            o.put("bookId", item.bookId)
            o.put("source", item.source)
            o.put("chapterNum", item.chapterNum)
            o.put("description", item.description)
            item.episodeId?.let { o.put("episodeId", it) }
            item.episodeThumbnailUrl?.let { o.put("episodeThumbnailUrl", it) }
            item.initialVideoUrl?.let { o.put("initialVideoUrl", it) }
            o.put("totalChapters", item.totalChapters)
            o.put("preferredHeight", item.preferredHeight)
            item.headers?.let { o.put("headers", JSONObject(it)) }
            item.subtitlesJson?.let { o.put("subtitlesJson", it) }
            item.streamKeysJson?.let { o.put("streamKeysJson", it) }
            item.audioUrl?.let { o.put("audioUrl", it) }
            o.put("isAudioPart", item.isAudioPart)
            item.drmScheme?.let { o.put("drmScheme", it) }
            item.drmLicenseUrl?.let { o.put("drmLicenseUrl", it) }
            item.drmBase64Key?.let { o.put("drmBase64Key", it) }
            arr.put(o)
        }
        return arr.toString()
    }

    fun decode(raw: String, coerceActiveToPaused: Boolean = true): List<DownloadItem> {
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        val items = mutableListOf<DownloadItem>()
        for (i in 0 until arr.length()) {
            parseItem(arr.optJSONObject(i), coerceActiveToPaused)?.let { items.add(it) }
        }
        return items
    }

    private fun parseItem(o: JSONObject?, coerceActiveToPaused: Boolean): DownloadItem? {
        if (o == null) return null
        return runCatching {
            val rawState = DownloadState.valueOf(o.getString("state"))
            val state = if (coerceActiveToPaused &&
                (rawState == DownloadState.DOWNLOADING || rawState == DownloadState.PENDING)
            ) {
                DownloadState.PAUSED
            } else {
                rawState
            }
            DownloadItem(
                id = o.getString("id"),
                url = o.getString("url"),
                title = o.optString("title", ""),
                episode = o.optString("episode", ""),
                quality = o.optString("quality", ""),
                state = state,
                bytesDownloaded = o.optLong("bytesDownloaded", 0),
                totalBytes = o.optLong("totalBytes", 0),
                error = o.optString("error").ifEmpty { null },
                contentUri = o.optString("contentUri").ifEmpty { null },
                filePath = o.optString("filePath").ifEmpty { null },
                addedAt = o.optLong("addedAt", 0),
                finishedAt = o.optLong("finishedAt", 0),
                posterUrl = o.optString("posterUrl", ""),
                bookId = o.optString("bookId", ""),
                source = o.optString("source", ""),
                chapterNum = o.optInt("chapterNum", 0),
                description = o.optString("description", ""),
                episodeId = o.optString("episodeId").ifEmpty { null },
                episodeThumbnailUrl = o.optString("episodeThumbnailUrl").ifEmpty { null },
                initialVideoUrl = o.optString("initialVideoUrl").ifEmpty { null },
                totalChapters = o.optInt("totalChapters", 0),
                preferredHeight = o.optInt("preferredHeight", 0),
                headers = parseHeaders(o.optJSONObject("headers")),
                subtitlesJson = o.optString("subtitlesJson").ifEmpty { null },
                streamKeysJson = o.optString("streamKeysJson").ifEmpty { null },
                audioUrl = o.optString("audioUrl").ifEmpty { null },
                isAudioPart = o.optBoolean("isAudioPart", false),
                drmScheme = o.optString("drmScheme").ifEmpty { null },
                drmLicenseUrl = o.optString("drmLicenseUrl").ifEmpty { null },
                drmBase64Key = o.optString("drmBase64Key").ifEmpty { null }
            )
        }.getOrNull()
    }

    private fun parseHeaders(o: JSONObject?): Map<String, String>? {
        if (o == null) return null
        val map = mutableMapOf<String, String>()
        val keys = o.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = o.optString(key)
        }
        return map.ifEmpty { null }
    }

    /** Progres kompak (id -> bytes/total) untuk persistensi berkala */
    fun encodeProgress(items: List<DownloadItem>): String {
        val o = JSONObject()
        items.forEach { item ->
            if (item.isActive || item.state == DownloadState.PAUSED && item.bytesDownloaded > 0) {
                o.put(item.id, JSONObject().put("b", item.bytesDownloaded).put("t", item.totalBytes))
            }
        }
        return o.toString()
    }

    fun overlayProgress(items: List<DownloadItem>, progressRaw: String?): List<DownloadItem> {
        if (progressRaw.isNullOrBlank()) return items
        val prog = runCatching { JSONObject(progressRaw) }.getOrNull() ?: return items
        return items.map { item ->
            val p = prog.optJSONObject(item.id) ?: return@map item
            val b = p.optLong("b", item.bytesDownloaded)
            val t = p.optLong("t", item.totalBytes)
            if (b > item.bytesDownloaded || t > item.totalBytes) {
                item.copy(bytesDownloaded = b, totalBytes = t)
            } else item
        }
    }
}
