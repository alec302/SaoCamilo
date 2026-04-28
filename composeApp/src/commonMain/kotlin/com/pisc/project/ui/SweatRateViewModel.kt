package com.pisc.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pisc.project.data.local.SweatRateEntity
import com.pisc.project.data.repository.SweatRateRepository
import com.pisc.project.getEpochTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class SweatRateResult(
    val hourlySweatRateL: Double,
    val totalFluidLossL: Double,
    val weightChangePercentage: Double,
    val targetIntakeMlPerHour: Int,
    val fractionationSuggestion: String,
    val overIntakeRisk: Boolean
)

class SweatRateViewModel(
    private val repository: SweatRateRepository,
    private val userEmail: String
) : ViewModel() {

    init {
        // Tenta sincronizar registros da nuvem pro local ao iniciar
        viewModelScope.launch {
            repository.syncCloudToLocal(userEmail)
        }
    }

    // 1. Estado para o cálculo atual
    private val _uiState = MutableStateFlow<SweatRateResult?>(null)
    val uiState: StateFlow<SweatRateResult?> = _uiState.asStateFlow()

    // 2. Estado para o Histórico (Lê direto do repositório escopado ao usuário)
    val history = repository.getAllSessions(userEmail)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onCalculateClicked(
        preWeightStr: String,
        postWeightStr: String,
        intakeStr: String,
        urineStr: String,
        durationStr: String,
        trainingType: String,
        climate: String
    ) {
        println("--- BOTÃO CLICADO ---")

        val preWeight = preWeightStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val postWeight = postWeightStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val intakeMl = intakeStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val urineMl = urineStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val durationMin = durationStr.replace(",", ".").toDoubleOrNull() ?: 60.0

        if (preWeight <= 0.0 || durationMin <= 0.0) {
            println("ERRO: Peso ou Tempo estão zerados. Abortando cálculo.")
            return
        }

        val weightLostKg = preWeight - postWeight
        val totalLossL = weightLostKg + (intakeMl / 1000.0) - (urineMl / 1000.0)
        val hourlyRate = totalLossL / (durationMin / 60.0)
        val weightChangePct = (weightLostKg / preWeight) * 100.0

        val targetIntake = (hourlyRate * 1000).coerceAtLeast(0.0).toInt()
        val frac = targetIntake / 4 // a cada 15 min

        val result = SweatRateResult(
            hourlySweatRateL = hourlyRate,
            totalFluidLossL = totalLossL,
            weightChangePercentage = weightChangePct,
            targetIntakeMlPerHour = targetIntake,
            fractionationSuggestion = "Beba ~${frac}mL a cada 15 minutos",
            overIntakeRisk = weightLostKg < 0
        )

        _uiState.update { result }

        println("Calculou com sucesso! Tentando salvar no repositório (Local + Cloud)...")

        viewModelScope.launch {
            try {
                val entity = SweatRateEntity(
                    userEmail = userEmail,
                    dateTimestamp = getEpochTime(),
                    initialWeight = preWeight,
                    finalWeight = postWeight,
                    intakeMl = intakeMl,
                    urineMl = urineMl,
                    durationMin = durationMin,
                    hourlyRateL = hourlyRate,
                    trainingType = trainingType,
                    climate = climate
                )
                repository.insertSession(entity)
                println("--- ENVIADO PARA O REPOSITÓRIO! ---")
            } catch (e: Exception) {
                println("ERRO NO REPOSITÓRIO: ${e.message}")
            }
        }
    }
}