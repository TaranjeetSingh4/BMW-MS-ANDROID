package com.appventurez.bmwms.repository

import com.appventurez.bmwms.model.LoginResponse
import com.appventurez.bmwms.network.ApiService
import retrofit2.Response

class LoginRepository(private val apiService: ApiService) {
    suspend fun login(url: String, params: Map<String, String>): Response<LoginResponse> {
        return apiService.login(url, params)
    }
}
