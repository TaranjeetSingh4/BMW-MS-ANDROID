package com.appventurez.bmwms.model

import com.appventurez.bmwms.cbwtf.models.ReportsModel
import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ReportsModel>?
)
