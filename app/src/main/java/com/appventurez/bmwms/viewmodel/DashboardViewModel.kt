package com.appventurez.bmwms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appventurez.bmwms.model.*
import com.appventurez.bmwms.repository.DashboardRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _noticeResponse = MutableLiveData<Response<NoticeResponse>>()
    val noticeResponse: LiveData<Response<NoticeResponse>> get() = _noticeResponse

    private val _cbwtfDataResponse = MutableLiveData<Response<CbwtfDataResponse>>()
    val cbwtfDataResponse: LiveData<Response<CbwtfDataResponse>> get() = _cbwtfDataResponse

    private val _todayDataResponse = MutableLiveData<Response<TodayDataResponse>>()
    val todayDataResponse: LiveData<Response<TodayDataResponse>> get() = _todayDataResponse

    private val _hospitalDataResponse = MutableLiveData<Response<HospitalDataResponse>>()
    val hospitalDataResponse: LiveData<Response<HospitalDataResponse>> get() = _hospitalDataResponse

    private val _otpResponse = MutableLiveData<Response<OtpResponse>>()
    val otpResponse: LiveData<Response<OtpResponse>> get() = _otpResponse

    private val _sendQueryResponse = MutableLiveData<Response<StatusResponse>>()
    val sendQueryResponse: LiveData<Response<StatusResponse>> get() = _sendQueryResponse

    private val _reportResponse = MutableLiveData<Response<ReportResponse>>()
    val reportResponse: LiveData<Response<ReportResponse>> get() = _reportResponse

    private val _sendMailResponse = MutableLiveData<Response<StatusResponse>>()
    val sendMailResponse: LiveData<Response<StatusResponse>> get() = _sendMailResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getNotice(url: String) {
        viewModelScope.launch {
            try {
                val response = repository.getNotice(url)
                _noticeResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getCbwtfData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getCbwtfData(url, params)
                _cbwtfDataResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getTodayData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getTodayData(url, params)
                _todayDataResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getHospitalData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getHospitalData(url, params)
                _hospitalDataResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun generateOtp(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.generateOtp(url, params)
                _otpResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun sendQuery(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.sendQuery(url, params)
                _sendQueryResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getReports(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getReports(url, params)
                _reportResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun sendMail(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.sendMail(url, params)
                _sendMailResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
