package com.cineflow.app.data.model

import com.google.gson.annotations.SerializedName

data class AppSessionRequest(
    @SerializedName("app_instance_id") val appInstanceId: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("app_version_name") val appVersionName: String,
    @SerializedName("app_version_code") val appVersionCode: Int,
    @SerializedName("apk_sha256") val apkSha256: String? = null,
    @SerializedName("signer_sha256") val signerSha256: String? = null,
    @SerializedName("device_type") val deviceType: String,
    @SerializedName("ui_mode") val uiMode: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("android_sdk_int") val androidSdkInt: Int,
    @SerializedName("android_release") val androidRelease: String,
    @SerializedName("locale") val locale: String,
    @SerializedName("requested_at_epoch_ms") val requestedAtEpochMs: Long
)

data class AppSessionResponseData(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Long? = null,
    @SerializedName("expires_at_epoch_ms") val expiresAtEpochMs: Long? = null,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("trust_level") val trustLevel: String? = null
)
