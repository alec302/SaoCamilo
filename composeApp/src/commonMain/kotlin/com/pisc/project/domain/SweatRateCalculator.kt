package com.pisc.project.domain

object SweatRateCalculator {
    fun calculate(session: SweatRateSession): SweatRateResult {
        // Perda de Massa Corporal Ajustada (L) = (Peso Pré - Peso Pós) + Ingestão - Urina
        // Nota: 1kg de massa corporal ~= 1L de fluido
        val intakeL = session.fluidIntakeMl / 1000.0
        val urineL = session.urineOutputMl / 1000.0

        val weightLossL = (session.initialWeightKg - session.finalWeightKg) + intakeL - urineL
        val durationHours = session.durationMinutes / 60.0

        val hourlyRate = if (durationHours > 0) weightLossL / durationHours else 0.0
        val percentageChange = ((session.initialWeightKg - session.finalWeightKg) / session.initialWeightKg) * 100

        return SweatRateResult(
            hourlySweatRateL = hourlyRate,
            totalFluidLossL = weightLossL,
            weightChangePercentage = percentageChange
        )
    }
}