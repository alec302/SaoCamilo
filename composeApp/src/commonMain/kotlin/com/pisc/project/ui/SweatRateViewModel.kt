package com.pisc.project.ui

import androidx.lifecycle.ViewModel
import com.pisc.project.domain.SweatRateCalculator
import com.pisc.project.domain.SweatRateSession
import com.pisc.project.domain.SweatRateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SweatRateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SweatRateResult?>(null)
    val uiState = _uiState.asStateFlow()

    fun onCalculateClicked(
        preWeight: String,
        postWeight: String,
        intake: String,
        urine: String,
        minutes: String
    ) {
        val session = SweatRateSession(
            initialWeightKg = preWeight.toDoubleOrNull() ?: 0.0,
            finalWeightKg = postWeight.toDoubleOrNull() ?: 0.0,
            fluidIntakeMl = intake.toDoubleOrNull() ?: 0.0,
            urineOutputMl = urine.toDoubleOrNull() ?: 0.0,
            durationMinutes = minutes.toDoubleOrNull() ?: 60.0
        )
        _uiState.update { SweatRateCalculator.calculate(session) }
    }
}