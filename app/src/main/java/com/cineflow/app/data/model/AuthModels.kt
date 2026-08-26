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
