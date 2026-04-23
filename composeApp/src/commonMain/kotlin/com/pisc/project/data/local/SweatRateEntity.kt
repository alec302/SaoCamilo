package com.pisc.project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sweat_sessions")
data class SweatRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTimestamp: Long, // Para sabermos QUANDO o treino aconteceu
    val initialWeight: Double,
    val finalWeight: Double,
    val intakeMl: Double,
    val urineMl: Double,
    val durationMin: Double,
    val hourlyRateL: Double // O resultado principal (L/h)
)