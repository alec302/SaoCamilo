package com.pisc.project.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class AppUserDto(
    val email: String,
    val passwordHash: String, // Armazenaremos a senha em texto claro apenas para fins didáticos/teste
    val isDarkTheme: Boolean = false
)

@Serializable
data class ThemeUpdateDto(
    val isDarkTheme: Boolean
)

class AuthApiService {
    
    private val client = SupabaseConfig.client

    suspend fun login(email: String, pass: String): AuthResponse {
        return try {
            // Busca o usuário batendo email e senha exata
            val users = client.postgrest["app_users"]
                .select { 
                    filter { 
                        eq("email", email) 
                        eq("passwordHash", pass)
                    } 
                }
                .decodeList<AppUserDto>()

            if (users.isNotEmpty()) {
                val user = users.first()
                AuthResponse(token = "real_token_${email}", userId = email, isDarkTheme = user.isDarkTheme)
            } else {
                AuthResponse(token = null, userId = null, error = "Credenciais inválidas. Verifique o e-mail e senha.")
            }
        } catch (e: Exception) {
            AuthResponse(token = null, userId = null, error = "Erro de conexão: ${e.message}")
        }
    }

    suspend fun signUp(email: String, pass: String): AuthResponse {
        return try {
            // Primeiro checa se já existe alguém com esse e-mail
            val existing = client.postgrest["app_users"]
                .select { filter { eq("email", email) } }
                .decodeList<AppUserDto>()
                
            if (existing.isNotEmpty()) {
                return AuthResponse(token = null, userId = null, error = "Este e-mail já está em uso!")
            }

            // Insere o novo usuário
            val newUser = AppUserDto(email = email, passwordHash = pass, isDarkTheme = false)
            client.postgrest["app_users"].insert(newUser)
            
            // Loga automaticamente
            AuthResponse(token = "real_token_${email}", userId = email, isDarkTheme = false)
        } catch (e: Exception) {
            AuthResponse(token = null, userId = null, error = "Erro ao criar conta: ${e.message}")
        }
    }

    suspend fun updateTheme(email: String, isDarkTheme: Boolean) {
        try {
            client.postgrest["app_users"].update(ThemeUpdateDto(isDarkTheme)) {
                filter { eq("email", email) }
            }
        } catch (e: Exception) {
            println("Erro ao salvar tema na nuvem: ${e.message}")
        }
    }

    suspend fun updateCredentials(oldEmail: String, newEmail: String, newPass: String): Boolean {
        return try {
            client.postgrest["app_users"].update(CredentialsUpdateDto(newEmail, newPass)) {
                filter { eq("email", oldEmail) }
            }
            true
        } catch (e: Exception) {
            println("Erro ao atualizar credenciais na nuvem: ${e.message}")
            false
        }
    }
}