package com.appventurez.bmwms.repository

import com.appventurez.bmwms.model.*
import com.appventurez.bmwms.network.ApiService
import retrofit2.Response

class DashboardRepository(private val apiService: ApiService) {
    suspend fun getNotice(url: String): Response<NoticeResponse> {
        return apiService.getNotice(url)
    }

    suspend fun getCbwtfData(url: String, params: Map<String, String>): Response<CbwtfDataResponse> {
        return apiService.getCbwtfData(url, params)
    }

    suspend fun getTodayData(url: String, params: Map<String, String>): Response<TodayDataResponse> {
        return apiService.getTodayData(url, params)
    }

    suspend fun getHospitalData(url: String, params: Map<String, String>): Response<HospitalDataResponse> {
        return apiService.getHospitalData(url, params)
    }

    suspend fun generateOtp(url: String, params: Map<String, String>): Response<OtpResponse> {
        return apiService.generateOtp(url, params)
    }

    suspend fun sendQuery(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.sendQuery(url, params)
    }

    suspend fun getReports(url: String, params: Map<String, String>): Response<ReportResponse> {
        return apiService.getReports(url, params)
    }

    suspend fun sendMail(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.sendMail(url, params)
    }
}
