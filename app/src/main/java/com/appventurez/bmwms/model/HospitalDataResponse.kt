package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class HospitalDataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<HospitalData>?
)

data class HospitalData(
    @SerializedName("name") val name: String?,
    @SerializedName("address") val address: String?
)
