package com.pisc.project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "sweat_sessions")
@Serializable
data class SweatRateEntity(
    @PrimaryKey val id: String = generateUUID(),
    val userEmail: String, // Identidade do usuário
    val dateTimestamp: Long, // Para sabermos QUANDO o treino aconteceu
    val initialWeight: Double,
    val finalWeight: Double,
    val intakeMl: Double,
    val urineMl: Double,
    val durationMin: Double,
    val hourlyRateL: Double, // O resultado principal (L/h)
    val isSynced: Boolean = false, // Controle Offline-First
    val trainingType: String = "",
    val climate: String = ""
)

fun generateUUID(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..16).map { chars.random() }.joinToString("")
}