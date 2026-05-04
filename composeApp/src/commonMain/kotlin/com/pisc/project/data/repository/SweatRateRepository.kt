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
    fun getAllSessions(userEmail: String): Flow<List<SweatRateEntity>> {
        return localDao.getAllSessions(userEmail)
    }

    suspend fun insertSession(session: SweatRateEntity) {
        localDao.insertSession(session.copy(isSynced = false))
        
        CoroutineScope(Dispatchers.Default).launch {
            val sessionToSync = session.copy(isSynced = false)
            val success = cloudDataSource.insertSession(sessionToSync)
            
            if (success) {
                localDao.updateSession(sessionToSync.copy(isSynced = true))
            }
        }
    }

    suspend fun syncCloudToLocal(userEmail: String) {
        // 1. Tenta baixar todos os dados do usuário da nuvem
        val cloudSessions = cloudDataSource.fetchSessions(userEmail)
        
        if (cloudSessions != null) {
            // Converte os DTOs em Entidades marcadas como sincronizadas
            val entities = cloudSessions.map { dto ->
                SweatRateEntity(
                    id = dto.id,
                    userEmail = dto.userEmail,
                    dateTimestamp = dto.dateTimestamp,
                    initialWeight = dto.initialWeight,
                    finalWeight = dto.finalWeight,
                    intakeMl = dto.intakeMl,
                    urineMl = dto.urineMl,
                    durationMin = dto.durationMin,
                    hourlyRateL = dto.hourlyRateL,
                    trainingType = dto.trainingType,
                    climate = dto.climate,
                    isSynced = true
                )
            }
            // Injeta no Room. Como os IDs são UUIDs, ele fará merge (REPLACE) sem duplicar.
            localDao.insertSessions(entities)
        }

        // 2. Agora pega os dados locais pendentes deste usuário e manda para a nuvem
        val pendingSessions = localDao.getUnsyncedSessions(userEmail)
        for (session in pendingSessions) {
            val success = cloudDataSource.insertSession(session)
            if (success) {
                localDao.updateSession(session.copy(isSynced = true))
            }
        }
    }

    suspend fun updateEmailLocally(oldEmail: String, newEmail: String) {
        localDao.updateUserEmail(oldEmail, newEmail)
    }
}
