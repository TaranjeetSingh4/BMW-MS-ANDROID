package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class TodayDataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<TodayWasteData>?
)

data class TodayWasteData(
    @SerializedName("qr_data_id") val qrDataId: String?,
    @SerializedName("qr_id") val qrId: String?,
    @SerializedName("hospital_code") val hospitalCode: String?,
    @SerializedName("operator_id") val operatorId: String?,
    @SerializedName("cbwtf_weight") val cbwtfWeight: String?,
    @SerializedName("hcf_weight") val hcfWeight: String?
)
