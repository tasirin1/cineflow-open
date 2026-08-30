package com.cineflow.app.data.model

import com.google.gson.annotations.SerializedName

data class AuthNonceRequest(
    @SerializedName("app_instance_id") val appInstanceId: String,
    @SerializedName("package_name") val packageName: String = "com.cineflow.app",
    @SerializedName("app_version_code") val appVersionCode: Int = 12
)

data class AuthNonceResponseData(
    @SerializedName("nonce") val nonce: String = "",
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int = 300
)

data class GoogleAccountLoginRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("nonce") val nonce: String,
    @SerializedName("app_instance_id") val appInstanceId: String,
    @SerializedName("device_type") val deviceType: String = "android_mobile",
    @SerializedName("ui_mode") val uiMode: String = "phone",
    @SerializedName("manufacturer") val manufacturer: String = "",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("model") val model: String = "",
    @SerializedName("android_sdk_int") val androidSdkInt: Int = 0,
    @SerializedName("android_release") val androidRelease: String = "",
    @SerializedName("locale") val locale: String = "",
    @SerializedName("integrity_token") val integrityToken: String? = null
)

data class AuthLoginResponseData(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("user") val user: AuthUserInfo? = null
)

data class AuthUserInfo(
    @SerializedName("id") val id: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

// ==================== Device Pairing (Login tanpa Google/SHA-1) ====================

data class DeviceLinkStartRequest(
    @SerializedName("app_instance_id") val appInstanceId: String,
    @SerializedName("package_name") val packageName: String = "com.cineflow.app",
    @SerializedName("app_version_code") val appVersionCode: Int = 12,
    @SerializedName("device_type") val deviceType: String = "android_mobile",
    @SerializedName("ui_mode") val uiMode: String = "phone",
    @SerializedName("manufacturer") val manufacturer: String = "",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("model") val model: String = "",
    @SerializedName("android_sdk_int") val androidSdkInt: Int = 0,
    @SerializedName("android_release") val androidRelease: String = "",
    @SerializedName("locale") val locale: String = ""
)

data class DeviceLinkStartResponseData(
    @SerializedName("device_code") val deviceCode: String? = null,
    @SerializedName("user_code") val userCode: String? = null,
    @SerializedName("verification_uri") val verificationUri: String? = null,
    @SerializedName("verification_uri_complete") val verificationUriComplete: String? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int = 900,
    @SerializedName("interval_seconds") val intervalSeconds: Int = 5,
    @SerializedName("status") val status: String? = null
)

data class DeviceLinkStatusData(
    @SerializedName("status") val status: String? = null,
    @SerializedName("approved") val approved: Boolean = false,
    @SerializedName("interval_seconds") val intervalSeconds: Int = 5,
    @SerializedName("is_authenticated") val isAuthenticated: Boolean = false,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("user") val user: AuthUserInfo? = null,
    @SerializedName("grant_token") val grantToken: String? = null
)

data class DeviceLinkExchangeRequest(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("app_instance_id") val appInstanceId: String,
    @SerializedName("grant_token") val grantToken: String
)

/**
 * Respons dari POST /api/app/auth/device/exchange.
 * Server membungkus token di dalam objek `data.token_info`
 * (bukan langsung di level `data`), berbeda dari login Google.
 */
data class DeviceLinkExchangeResponseData(
    @SerializedName("token_info") val tokenInfo: DeviceLinkTokenInfo? = null,
    @SerializedName("user") val user: AuthUserInfo? = null
)

data class DeviceLinkTokenInfo(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null
)
