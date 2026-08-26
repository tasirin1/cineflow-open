package com.cineflow.app.data.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("data") val data: T? = null
) {
    val isSuccess: Boolean get() = code == 200
}

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class CategoryResponseData(
    @SerializedName("model_id") val modelId: String? = null,
    @SerializedName("data") val categories: List<Category> = emptyList()
)

data class VideoItem(
    @SerializedName("key") val key: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("cover_is_landscape") val coverIsLandscape: Boolean? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("desc") val description: String? = null,
    @SerializedName("labels") val labels: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("tag") val tags: List<String>? = null,
    @SerializedName("series_tag") val seriesTags: List<String>? = null,
    @SerializedName("episode_count") val episodeCount: Int? = null,
    @SerializedName("follow_count") val followCount: Int? = null,
    @SerializedName("hot_score") val hotScore: String? = null,
    @SerializedName("source") val source: String? = null
)

data class VideoListResponseData(
    @SerializedName("type") val type: String? = null,
    @SerializedName("module_type") val moduleType: String? = null,
    @SerializedName("module_name") val moduleName: String? = null,
    @SerializedName("items") val items: List<VideoItem>? = null,
    @SerializedName("cover_is_landscape") val coverIsLandscape: Boolean? = null
)

data class UnifiedDetailResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("cover_url") val coverUrl: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("total_episodes") val totalEpisodes: Int = 0,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("genres") val genres: List<String> = emptyList(),
    @SerializedName("seasons") val seasons: List<UnifiedSeason> = emptyList(),
    @SerializedName("download") val download: Boolean? = null
) {
    val allEpisodes: List<UnifiedEpisode>
        get() = seasons.flatMap { it.episodes }
}

data class UnifiedSeason(
    @SerializedName("name") val name: String,
    @SerializedName("index") val index: Int = 0,
    @SerializedName("episodes") val episodes: List<UnifiedEpisode>
)

data class UnifiedEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("number") val number: Int,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("duration_seconds") val durationSeconds: Long = 0,
    @SerializedName("is_vip") val isVip: Boolean = false,
    @SerializedName("subtitles") val subtitles: List<UnifiedSubtitle>? = null,
    @SerializedName("label") val label: String? = null
) {
    val displayLabel: String get() = if (label.isNullOrBlank()) number.toString() else label
}

data class UnifiedSubtitle(
    @SerializedName("url") val url: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("label") val label: String? = null
)

data class UnifiedVideoSourceResponse(
    @SerializedName("episode_id") val episodeId: String = "",
    @SerializedName("streams") val streams: List<UnifiedStream> = emptyList(),
    @SerializedName("subtitles") val subtitles: List<UnifiedSubtitle> = emptyList()
) {
    fun sanitized(): UnifiedVideoSourceResponse = copy(
        episodeId = episodeId.ifBlank { "" },
        streams = streams.filterNotNull(),
        subtitles = subtitles.filterNotNull()
    )
}

data class UnifiedStream(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("server") val server: String? = null,
    @SerializedName("quality_code") val qualityCode: String? = null,
    @SerializedName("master_url") val masterUrl: String? = null,
    @SerializedName("format") val format: String? = "mp4",
    @SerializedName("headers") val headers: Map<String, String>? = null,
    @SerializedName("video") val video: List<String>? = null,
    @SerializedName("audio") val audio: List<String>? = null,
    @SerializedName("is_drm") val isDrm: Boolean? = null,
    @SerializedName("drm_type") val drmType: String? = null,
    @SerializedName("drm") val drm: UnifiedDrmInfo? = null
)

data class UnifiedDrmInfo(
    @SerializedName("scheme") val scheme: String? = null,
    @SerializedName("license_url") val licenseUrl: String? = null,
    @SerializedName("headers") val headers: Map<String, String>? = null
)

data class SearchRequest(
    @SerializedName("q") val query: String,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("model_id") val modelId: String? = null,
    @SerializedName("content_type") val contentType: String? = null
)

data class SearchResponseData(
    @SerializedName("query") val query: String? = null,
    @SerializedName("items") val items: List<VideoItem> = emptyList()
)

data class AuthLoginResponseData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class AuthCurrentUserData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("is_premium") val isPremium: Boolean = false
)

data class AuthRefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class AuthRefreshTokenResponseData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)
