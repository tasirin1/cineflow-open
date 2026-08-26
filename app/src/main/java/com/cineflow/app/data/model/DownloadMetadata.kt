package com.cineflow.app.data.model

import com.google.gson.annotations.SerializedName

data class DownloadMetadata(
    @SerializedName("title") val title: String,
    @SerializedName("episode") val episode: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("poster_url") val posterUrl: String,
    @SerializedName("book_id") val bookId: String,
    @SerializedName("source") val source: String,
    @SerializedName("chapter_num") val chapterNum: Int = 0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("subtitle_path") val subtitlePath: String? = null,
    @SerializedName("subtitles_json") val subtitlesJson: String? = null,
    @SerializedName("local_poster_path") val localPosterPath: String? = null,
    @SerializedName("local_episode_thumbnail_path") val localEpisodeThumbnailPath: String? = null,
    @SerializedName("total_chapters") val totalChapters: Int = 0,
    @SerializedName("preferred_height") val preferredHeight: Int = 0,
    @SerializedName("headers") val headers: Map<String, String>? = null,
    @SerializedName("episode_thumbnail_url") val episodeThumbnailUrl: String? = null,
    @SerializedName("initial_video_url") val initialVideoUrl: String? = null,
    @SerializedName("episode_id") val episodeId: String? = null,
    @SerializedName("stream_keys_json") val streamKeysJson: String? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("is_audio_part") val isAudioPart: Boolean = false,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("drm") val drm: DrmInfoData? = null,
    @SerializedName("preferred_server_url") val preferredServerUrl: String? = null,
    @SerializedName("preferred_server_format") val preferredServerFormat: String? = null,
    @SerializedName("preferred_server_group_key") val preferredServerGroupKey: String? = null
)
