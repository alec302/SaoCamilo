package com.pisc.project.domain

data class SweatRateSession(
    val initialWeightKg: Double,
    val finalWeightKg: Double,
    val fluidIntakeMl: Double,
    val urineOutputMl: Double,
    val durationMinutes: Double
)

data class SweatRateResult(
    val hourlySweatRateL: Double,
    val totalFluidLossL: Double,
    val weightChangePercentage: Double
)

