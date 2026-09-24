package com.appventurez.bmwms.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<UserData>?
)

data class UserData(
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("operator_id") val operatorId: String?,
    @SerializedName("hospital_code") val hospitalCode: String?,
    @SerializedName("cbwtf_id") val cbwtfId: String?,
    @SerializedName("mobile") val mobile: String?
)
