package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class CbwtfDataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<CbwtfData>?
)

data class CbwtfData(
    @SerializedName("name") val name: String?,
    @SerializedName("attendace_compulsory") val attendanceCompulsory: String?
)
