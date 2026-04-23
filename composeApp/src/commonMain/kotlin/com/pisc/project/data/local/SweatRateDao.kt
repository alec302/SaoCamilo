package com.pisc.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SweatRateDao {

    // Função para salvar um novo treino
    @Insert
    suspend fun insertSession(session: SweatRateEntity)

    // Função para puxar o histórico do mais recente para o mais antigo
    @Query("SELECT * FROM sweat_sessions ORDER BY dateTimestamp DESC")
    fun getAllSessions(): Flow<List<SweatRateEntity>>
}