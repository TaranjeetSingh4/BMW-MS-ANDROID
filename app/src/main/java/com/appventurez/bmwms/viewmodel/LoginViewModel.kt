package com.appventurez.bmwms.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appventurez.bmwms.model.LoginResponse
import com.appventurez.bmwms.repository.LoginRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginResponse = mutableStateOf<Response<LoginResponse>?>(null)
    val loginResponse: State<Response<LoginResponse>?> = _loginResponse

    fun login(url: String, params: Map<String, String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.login(url, params)
                _loginResponse.value = response
            } catch (e: Exception) {
                _loginResponse.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetLoginResponse() {
        _loginResponse.value = null
    }
}
