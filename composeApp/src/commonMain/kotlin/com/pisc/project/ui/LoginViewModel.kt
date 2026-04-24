package com.pisc.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pisc.project.data.remote.AuthApiService
import com.pisc.project.data.remote.networkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Instancia o serviço que criamos antes
    private val apiService = AuthApiService(networkClient)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun onLoginClicked(email: String, pass: String) {
        viewModelScope.launch {
            val response = apiService.login(email, pass)
            if (response.token != null) {
                _isLoggedIn.value = true
                _error.value = null
            } else {
                _error.value = response.error ?: "Erro ao fazer login"
            }
        }
    }
}