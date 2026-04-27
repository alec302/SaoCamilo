package com.pisc.project.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg: Color,
    val surface: Color,
    val accentStart: Color,
    val accentEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val danger: Color,
    val dangerBg: Color,
    val warning: Color,
    val warningBg: Color,
    val success: Color,
    val successBg: Color
) {
    val accentBrush = Brush.horizontalGradient(listOf(accentStart, accentEnd))
}

val DarkColors = AppColors(
    bg = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    accentStart = Color(0xFFFF5722),
    accentEnd = Color(0xFFD50000),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    danger = Color(0xFFFF5722),
    dangerBg = Color(0xFF3F0A0A),
    warning = Color(0xFF38BDF8),
    warningBg = Color(0xFF0C4A6E),
    success = Color(0xFF10B981),
    successBg = Color(0xFF10B981).copy(alpha = 0.2f)
)

val LightColors = AppColors(
    bg = Color(0xFFF1F5F9), // Slate muito claro
    surface = Color(0xFFFFFFFF), // Branco
    accentStart = Color(0xFFFF5722), // Mantém o gradiente performance
    accentEnd = Color(0xFFD50000),
    textPrimary = Color(0xFF0F172A), // Slate escuro para texto
    textSecondary = Color(0xFF64748B),
    danger = Color(0xFFD50000),
    dangerBg = Color(0xFFFEE2E2),
    warning = Color(0xFF0284C7),
    warningBg = Color(0xFFE0F2FE),
    success = Color(0xFF059669),
    successBg = Color(0xFFD1FAE5)
)

val LocalAppColors = staticCompositionLocalOf { DarkColors }
