package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class ScanItemRequest(
    @SerializedName("barcodeID") val barcodeId: Int?,
    @SerializedName("hospital_id") val hospitalId: String?,
    @SerializedName("seq_no") val seqNo: String?,
    @SerializedName("operator_name") val operatorName: String?,
    @SerializedName("admin_id") val adminId: String?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("type") val type: String?,
    @SerializedName("year") val year: String?,
    @SerializedName("handhover_address") val handhoverAddress: String?,
    @SerializedName("attenden_status") val attendenStatus: String?
)
