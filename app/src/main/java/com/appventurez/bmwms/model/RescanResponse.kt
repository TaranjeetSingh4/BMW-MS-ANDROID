package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class RescanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: RescanData?
)

data class RescanData(
    @SerializedName("hospital_code") val hospitalCode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("color_type_hcf") val colorTypeHcf: String?,
    @SerializedName("cbwtf_weight") val cbwtfWeight: String?,
    @SerializedName("hcf_weight") val hcfWeight: String?,
    @SerializedName("cbwtf_id") val cbwtfId: String?
)
