package com.pisc.project.data.remote

import com.pisc.project.data.local.SweatRateEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

object SupabaseConfig {
    const val SUPABASE_URL = "https://qudknwslvtbrlwmneqce.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF1ZGtud3NsdnRicmx3bW5lcWNlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxNTU5MzcsImV4cCI6MjA5MjczMTkzN30.VZNorGr_cRMKXmcBI21LSjMKhJI2voag6_-AWG_Eybo"  
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
        }
    }
}

class CloudSweatRateDataSource {
    
    private val client = SupabaseConfig.client
    
    suspend fun insertSession(session: SweatRateEntity): Boolean {
        return try {
            val dto = SessionDto(
                id = session.id,
                userEmail = session.userEmail,
                dateTimestamp = session.dateTimestamp,
                initialWeight = session.initialWeight,
                finalWeight = session.finalWeight,
                intakeMl = session.intakeMl,
                urineMl = session.urineMl,
                durationMin = session.durationMin,
                hourlyRateL = session.hourlyRateL,
                trainingType = session.trainingType,
                climate = session.climate
            )
            client.postgrest["sweat_sessions"].insert(dto)
            true
        } catch (e: Exception) {
            println("Erro ao salvar na nuvem: ${e.message}")
            false
        }
    }

    suspend fun fetchSessions(userEmail: String): List<SessionDto>? {
        return try {
            client.postgrest["sweat_sessions"]
                .select { filter { eq("userEmail", userEmail) } }
                .decodeList<SessionDto>()
        } catch (e: Exception) {
            println("Erro ao puxar da nuvem: ${e.message}")
            null
        }
    }

    suspend fun updateUserEmail(oldEmail: String, newEmail: String): Boolean {
        return try {
            @Serializable
            data class EmailUpdateDto(val userEmail: String)
            
            client.postgrest["sweat_sessions"].update(EmailUpdateDto(newEmail)) {
                filter { eq("userEmail", oldEmail) }
            }
            true
        } catch (e: Exception) {
            println("Erro ao atualizar email na nuvem: ${e.message}")
            false
        }
    }
}

@Serializable
data class SessionDto(
    val id: String,
    val userEmail: String,
    val dateTimestamp: Long,
    val initialWeight: Double,
    val finalWeight: Double,
    val intakeMl: Double,
    val urineMl: Double,
    val durationMin: Double,
    val hourlyRateL: Double,
    val trainingType: String,
    val climate: String
)
