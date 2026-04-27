package com.pisc.project.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.HttpClient

class AuthApiService(private val client: HttpClient) {

    suspend fun login(email: String, password: String): AuthResponse {
        // Simulando um delay de rede para ficar realista
        kotlinx.coroutines.delay(1500)

        // Simulação de servidor
        return if (email == "admin@saocamilo.com" && password == "senha123") {
            AuthResponse(token = "fake_token_123", userId = "user_01")
        } else {
            AuthResponse(token = null, userId = null, error = "Credenciais inválidas. Use admin@saocamilo.com / senha123")
        }
    }
}