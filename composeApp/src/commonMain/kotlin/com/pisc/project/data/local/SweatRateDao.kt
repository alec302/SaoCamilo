package com.pisc.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SweatRateDao {

    // Função para salvar um novo treino (retorna o ID gerado)
    @Insert
    suspend fun insertSession(session: SweatRateEntity): Long

    // Atualiza um treino existente (útil para marcar como sincronizado)
    @Update
    suspend fun updateSession(session: SweatRateEntity)

    // Busca apenas os treinos que ainda não foram para a nuvem
    @Query("SELECT * FROM sweat_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<SweatRateEntity>

    // Função para puxar o histórico do mais recente para o mais antigo
    @Query("SELECT * FROM sweat_sessions ORDER BY dateTimestamp DESC")
    fun getAllSessions(): Flow<List<SweatRateEntity>>
}