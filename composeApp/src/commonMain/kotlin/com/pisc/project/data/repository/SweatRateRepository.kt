package com.pisc.project.data.repository

import com.pisc.project.data.local.SweatRateDao
import com.pisc.project.data.local.SweatRateEntity
import com.pisc.project.data.remote.CloudSweatRateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SweatRateRepository(
    private val localDao: SweatRateDao,
    private val cloudDataSource: CloudSweatRateDataSource
) {
    // Retorna direto do banco local para ser offline-first e super rápido
    fun getAllSessions(): Flow<List<SweatRateEntity>> {
        return localDao.getAllSessions()
    }

    suspend fun insertSession(session: SweatRateEntity) {
        // 1. Salva localmente primeiro com isSynced = false
        val insertedId = localDao.insertSession(session.copy(isSynced = false))
        
        // 2. Tenta sincronizar com a nuvem em background
        CoroutineScope(Dispatchers.Default).launch {
            val sessionToSync = session.copy(id = insertedId, isSynced = false)
            val success = cloudDataSource.insertSession(sessionToSync)
            
            if (success) {
                // 3. Se deu certo, atualiza o status local para verdadeiro
                localDao.updateSession(sessionToSync.copy(isSynced = true))
            }
        }
    }

    suspend fun syncPendingSessions() {
        // Busca todos que falharam em subir antes (quando estava offline)
        val pendingSessions = localDao.getUnsyncedSessions()
        
        for (session in pendingSessions) {
            val success = cloudDataSource.insertSession(session)
            if (success) {
                localDao.updateSession(session.copy(isSynced = true))
            }
        }
    }
}
