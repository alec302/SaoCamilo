package com.pisc.project.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val pass: String // Use nomes que batam com a sua API futura
)

@Serializable
data class AuthResponse(
    val token: String?,
    val userId: String?,
    val error: String? = null
)