package com.appventurez.bmwms.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appventurez.bmwms.model.AppVersionResponse
import com.appventurez.bmwms.model.LoginResponse
import com.appventurez.bmwms.repository.SplashRepository
import com.appventurez.bmwms.utils.CrashlyticsHelper
import kotlinx.coroutines.launch
import retrofit2.Response

class SplashViewModel(private val repository: SplashRepository) : ViewModel() {
    private val TAG = "SplashViewModel"
    private val _loginResponse = MutableLiveData<Response<LoginResponse>>()
    val loginResponse: LiveData<Response<LoginResponse>> get() = _loginResponse

    private val _versionResponse = MutableLiveData<Response<AppVersionResponse>>()
    val versionResponse: LiveData<Response<AppVersionResponse>> get() = _versionResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun login(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.login(url, params)
                _loginResponse.value = response
            } catch (e: Exception) {
                CrashlyticsHelper.log("Splash login error: ${e.message}")
                CrashlyticsHelper.recordException(e)
                Log.e(TAG, "Error: ${e.message}")
                _error.value = e.message
            }
        }
    }

    fun getAppVersion(url: String) {
        viewModelScope.launch {
            try {
                val response = repository.getAppVersion(url)
                _versionResponse.value = response
            } catch (e: Exception) {
                CrashlyticsHelper.log("getAppVersion error: ${e.message}")
                CrashlyticsHelper.recordException(e)
                _error.value = e.message
            }
        }
    }
}
