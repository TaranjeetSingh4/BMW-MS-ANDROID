package com.appventurez.bmwms.network

import com.appventurez.bmwms.model.LoginResponse
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
}
