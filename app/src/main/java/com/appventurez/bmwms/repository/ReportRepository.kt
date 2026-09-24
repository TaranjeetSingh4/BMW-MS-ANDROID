package com.appventurez.bmwms.repository

import com.appventurez.bmwms.network.ApiService
import com.appventurez.bmwms.model.ReportResponse
import com.appventurez.bmwms.model.StatusResponse
import retrofit2.Response

class ReportRepository(private val apiService: ApiService) {

    suspend fun getReports(url: String, params: Map<String, String>): Response<ReportResponse> {
        return apiService.getReports(url, params)
    }

    suspend fun sendMail(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.sendMail(url, params)
    }
}
