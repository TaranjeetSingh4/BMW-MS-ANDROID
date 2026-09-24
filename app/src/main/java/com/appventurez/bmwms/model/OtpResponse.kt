package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class OtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<OtpData>?
)

data class OtpData(
    @SerializedName("otp") val otp: String?
)
