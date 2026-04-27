package com.pisc.project.data.remote

import com.pisc.project.data.local.SweatRateEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

object SupabaseConfig {
    // Derivado da string PostgreSQL fornecida pelo usuário:
    const val SUPABASE_URL = "https://qudknwslvtbrlwmneqce.supabase.co"
    // AVISO DE SEGURANÇA: NUNCA use a senha do banco (PostgreSQL) diretamente no App.
    // Você precisa fornecer a Anon Key, que você encontra em Settings -> API no painel do Supabase.
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
            // Convertemos para DTO para não enviar a flag local 'isSynced' para a nuvem
            val dto = SessionDto(
                id = session.id, // Comente esta linha se o Supabase gerar o ID automaticamente (auto-increment)
                dateTimestamp = session.dateTimestamp,
                initialWeight = session.initialWeight,
                finalWeight = session.finalWeight,
                intakeMl = session.intakeMl,
                urineMl = session.urineMl,
                durationMin = session.durationMin,
                hourlyRateL = session.hourlyRateL
            )
            client.postgrest["sweat_sessions"].insert(dto)
            true
        } catch (e: Exception) {
            println("Erro ao salvar na nuvem: ${e.message}")
            false
        }
    }
}

@Serializable
data class SessionDto(
    val id: Long,
    val dateTimestamp: Long,
    val initialWeight: Double,
    val finalWeight: Double,
    val intakeMl: Double,
    val urineMl: Double,
    val durationMin: Double,
    val hourlyRateL: Double
)
