package com.cineflow.app.data.model

import com.google.gson.annotations.SerializedName

data class DrmInfoData(
    @SerializedName("scheme") val scheme: String? = null,
    @SerializedName("license_url") val licenseUrl: String? = null,
    @SerializedName("headers") val headers: Map<String, String>? = null,
    @SerializedName("base64_key") val base64Key: String? = null
)
