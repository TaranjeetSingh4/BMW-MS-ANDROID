package com.appventurez.bmwms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appventurez.bmwms.model.*
import com.appventurez.bmwms.repository.ScanRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class ScanViewModel(private val repository: ScanRepository) : ViewModel() {

    private val _hospitalDataResponse = MutableLiveData<Response<HospitalDataResponse>>()
    val hospitalDataResponse: LiveData<Response<HospitalDataResponse>> get() = _hospitalDataResponse

    private val _qrDataResponse = MutableLiveData<Response<QrDataResponse>>()
    val qrDataResponse: LiveData<Response<QrDataResponse>> get() = _qrDataResponse

    private val _submitResponse = MutableLiveData<Response<StatusResponse>>()
    val submitResponse: LiveData<Response<StatusResponse>> get() = _submitResponse

    private val _otpResponse = MutableLiveData<Response<OtpResponse>>()
    val otpResponse: LiveData<Response<OtpResponse>> get() = _otpResponse

    private val _attendanceResponse = MutableLiveData<Response<StatusResponse>>()
    val attendanceResponse: LiveData<Response<StatusResponse>> get() = _attendanceResponse

    private val _locationResponse = MutableLiveData<Response<StatusResponse>>()
    val locationResponse: LiveData<Response<StatusResponse>> get() = _locationResponse

    private val _rescanResponse = MutableLiveData<Response<RescanResponse>>()
    val rescanResponse: LiveData<Response<RescanResponse>> get() = _rescanResponse

    private val _deleteResponse = MutableLiveData<Response<StatusResponse>>()
    val deleteResponse: LiveData<Response<StatusResponse>> get() = _deleteResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

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

    fun getQrData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getQrData(url, params)
                _qrDataResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun submitScan(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.submitScan(url, params)
                _submitResponse.value = response
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

    fun getOtp(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getOtp(url, params)
                _otpResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun checkAttendance(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.checkAttendance(url, params)
                _attendanceResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markAttendance(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.markAttendance(url, params)
                _attendanceResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun setHospitalLocation(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.setHospitalLocation(url, params)
                _locationResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getRescanData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.getRescanData(url, params)
                _rescanResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteQrData(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.deleteQrData(url, params)
                _deleteResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
