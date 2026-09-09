package com.appventurez.bmwms.repository

import com.appventurez.bmwms.model.AppVersionResponse
import com.appventurez.bmwms.model.LoginResponse
import com.appventurez.bmwms.network.ApiService
import retrofit2.Response

class SplashRepository(private val apiService: ApiService) {
    suspend fun login(url: String, params: Map<String, String>): Response<LoginResponse> {
        return apiService.login(url, params)
    }

    suspend fun getAppVersion(url: String): Response<AppVersionResponse> {
        return apiService.getAppVersion(url)
    }
}
