package com.appventurez.bmwms.network

import com.appventurez.bmwms.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST
    fun loginJava(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Call<LoginResponse>

    @POST
    suspend fun getAppVersion(@Url url: String): Response<AppVersionResponse>

    @POST
    suspend fun getNotice(@Url url: String): Response<NoticeResponse>

    @FormUrlEncoded
    @POST
    suspend fun getCbwtfData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<CbwtfDataResponse>

    @FormUrlEncoded
    @POST
    suspend fun getTodayData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<TodayDataResponse>

    @FormUrlEncoded
    @POST
    suspend fun getHospitalData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<HospitalDataResponse>

    @FormUrlEncoded
    @POST
    suspend fun generateOtp(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<OtpResponse>

    @FormUrlEncoded
    @POST
    suspend fun sendQuery(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun getReports(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<ReportResponse>

    @FormUrlEncoded
    @POST
    suspend fun sendMail(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun getQrData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<QrDataResponse>

    @FormUrlEncoded
    @POST
    suspend fun submitScan(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun getOtp(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<OtpResponse>

    @FormUrlEncoded
    @POST
    suspend fun checkAttendance(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun markAttendance(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun setHospitalLocation(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST
    suspend fun getRescanData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<RescanResponse>

    @FormUrlEncoded
    @POST
    suspend fun deleteQrData(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<StatusResponse>
}
