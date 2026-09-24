package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?
)
