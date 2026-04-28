package com.pisc.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pisc.project.data.remote.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Instancia o serviço
    private val apiService = AuthApiService()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode = _isSignUpMode.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _loggedInEmail = MutableStateFlow<String?>(null)
    val loggedInEmail = _loggedInEmail.asStateFlow()

    private val _loggedInTheme = MutableStateFlow(false)
    val loggedInTheme = _loggedInTheme.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun toggleMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _error.value = null // Limpa erros ao trocar de tela
    }

    fun onActionClicked(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Por favor, preencha todos os campos."
            return
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
        if (!emailRegex.matches(email.trim())) {
            _error.value = "Por favor, insira um formato de email válido."
            return
        }

        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val response = if (_isSignUpMode.value) {
                apiService.signUp(email.trim(), pass)
            } else {
                apiService.login(email.trim(), pass)
            }

            _isLoading.value = false
            if (response.token != null) {
                _loggedInTheme.value = response.isDarkTheme
                _loggedInEmail.value = email.trim()
                _isLoggedIn.value = true
                _error.value = null
            } else {
                _error.value = response.error ?: "Erro ao fazer login"
            }
        }
    }
}