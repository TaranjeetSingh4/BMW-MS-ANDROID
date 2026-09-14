package com.appventurez.bmwms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appventurez.bmwms.model.ReportResponse
import com.appventurez.bmwms.model.StatusResponse
import com.appventurez.bmwms.repository.ReportRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _reportResponse = MutableLiveData<Response<ReportResponse>>()
    val reportResponse: LiveData<Response<ReportResponse>> get() = _reportResponse

    private val _sendMailResponse = MutableLiveData<Response<StatusResponse>>()
    val sendMailResponse: LiveData<Response<StatusResponse>> get() = _sendMailResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getReports(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getReports(url, params)
                _reportResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMail(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.sendMail(url, params)
                _sendMailResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
