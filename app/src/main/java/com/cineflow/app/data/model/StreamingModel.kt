package com.cineflow.app.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreamingModel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("premium") val premium: Boolean = false,
    @SerializedName("content_type") val contentType: String? = null
) : Parcelable
