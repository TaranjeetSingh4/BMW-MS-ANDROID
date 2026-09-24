package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class QrDataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<QrData>?
)

data class QrData(
    @SerializedName("qr_id") val qrId: String?,
    @SerializedName("type") val type: String?
)
