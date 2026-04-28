package com.pisc.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface SweatRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SweatRateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SweatRateEntity>)

    @Update
    suspend fun updateSession(session: SweatRateEntity)

    @Query("SELECT * FROM sweat_sessions WHERE isSynced = 0 AND userEmail = :email")
    suspend fun getUnsyncedSessions(email: String): List<SweatRateEntity>

    @Query("SELECT * FROM sweat_sessions WHERE userEmail = :email ORDER BY dateTimestamp DESC")
    fun getAllSessions(email: String): Flow<List<SweatRateEntity>>
}