package com.appventurez.bmwms.repository

import com.appventurez.bmwms.model.*
import com.appventurez.bmwms.network.ApiService
import retrofit2.Response

class ScanRepository(private val apiService: ApiService) {
    suspend fun getHospitalData(url: String, params: Map<String, String>): Response<HospitalDataResponse> {
        return apiService.getHospitalData(url, params)
    }

    suspend fun getQrData(url: String, params: Map<String, String>): Response<QrDataResponse> {
        return apiService.getQrData(url, params)
    }

    suspend fun submitScan(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.submitScan(url, params)
    }

    suspend fun generateOtp(url: String, params: Map<String, String>): Response<OtpResponse> {
        return apiService.generateOtp(url, params)
    }

    suspend fun getOtp(url: String, params: Map<String, String>): Response<OtpResponse> {
        return apiService.getOtp(url, params)
    }

    suspend fun checkAttendance(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.checkAttendance(url, params)
    }

    suspend fun markAttendance(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.markAttendance(url, params)
    }

    suspend fun setHospitalLocation(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.setHospitalLocation(url, params)
    }

    suspend fun getRescanData(url: String, params: Map<String, String>): Response<RescanResponse> {
        return apiService.getRescanData(url, params)
    }

    suspend fun deleteQrData(url: String, params: Map<String, String>): Response<StatusResponse> {
        return apiService.deleteQrData(url, params)
    }
}
