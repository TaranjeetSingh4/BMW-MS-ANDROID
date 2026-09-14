package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class NoticeResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<NoticeData>?
)

data class NoticeData(
    @SerializedName("notice_url") val noticeUrl: String?,
    @SerializedName("notice_status") val noticeStatus: String?
)
