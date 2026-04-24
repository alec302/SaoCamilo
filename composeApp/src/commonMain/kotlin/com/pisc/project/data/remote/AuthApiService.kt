package com.pisc.project.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.HttpClient

class AuthApiService(private val client: HttpClient) {

    suspend fun login(email: String, password: String): AuthResponse {
/*        return try {
            val response = client.post("https://sua-api.com/login") { // Troque pela sua URL
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            response.body()
        } catch (e: Exception) {
            AuthResponse(token = null, userId = null, error = e.message)
        }*/
        // Simulando um delay de rede para ficar realista
        kotlinx.coroutines.delay(1000)

        // Retorna um token "fake" para o ViewModel achar que deu certo
        return AuthResponse(token = "fake_token_123", userId = "user_01")
    }
}