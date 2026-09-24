package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class AppVersionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<VersionData>?
)

data class VersionData(
    @SerializedName("version") val version: String?
)
