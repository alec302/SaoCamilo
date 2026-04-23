package com.pisc.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pisc.project.data.local.SweatRateDao
import com.pisc.project.data.local.SweatRateEntity
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
    val weightChangePercentage: Double
)

class SweatRateViewModel(private val dao: SweatRateDao) : ViewModel() {

    // 1. Estado para o cálculo atual
    private val _uiState = MutableStateFlow<SweatRateResult?>(null)
    val uiState: StateFlow<SweatRateResult?> = _uiState.asStateFlow()

    // 👇 AQUI ESTÁ A NOVIDADE 👇
    // 2. Estado para o Histórico (Lê direto do banco de dados automaticamente)
    val history = dao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // 👆 AQUI ESTÁ A NOVIDADE 👆

    fun onCalculateClicked(
        preWeightStr: String,
        postWeightStr: String,
        intakeStr: String,
        urineStr: String,
        durationStr: String
    ) {
        println("--- BOTÃO CLICADO ---")

        val preWeight = preWeightStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val postWeight = postWeightStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val intakeMl = intakeStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val urineMl = urineStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val durationMin = durationStr.replace(",", ".").toDoubleOrNull() ?: 60.0

        println("Dados lidos: $preWeight kg, $durationMin min")

        if (preWeight <= 0.0 || durationMin <= 0.0) {
            println("ERRO: Peso ou Tempo estão zerados. Abortando cálculo.")
            return
        }

        val weightLostKg = preWeight - postWeight
        val totalLossL = weightLostKg + (intakeMl / 1000.0) - (urineMl / 1000.0)
        val hourlyRate = totalLossL / (durationMin / 60.0)
        val weightChangePct = (weightLostKg / preWeight) * 100.0

        val result = SweatRateResult(
            hourlySweatRateL = hourlyRate,
            totalFluidLossL = totalLossL,
            weightChangePercentage = weightChangePct
        )

        _uiState.update { result }

        println("Calculou com sucesso! Tentando salvar no banco...")

        viewModelScope.launch {
            try {
                val entity = SweatRateEntity(
                    dateTimestamp = System.currentTimeMillis(),
                    initialWeight = preWeight,
                    finalWeight = postWeight,
                    intakeMl = intakeMl,
                    urineMl = urineMl,
                    durationMin = durationMin,
                    hourlyRateL = hourlyRate
                )
                dao.insertSession(entity)
                println("--- SALVO NO SQLITE COM SUCESSO! ---")
            } catch (e: Exception) {
                println("ERRO NO BANCO: ${e.message}")
            }
        }
    }
}