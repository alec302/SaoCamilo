package com.pisc.project.util

import com.pisc.project.data.local.SweatRateEntity

actual fun exportHistoryToPdf(
    history: List<SweatRateEntity>, 
    userEmail: String, 
    onSuccess: (String) -> Unit, 
    onError: (String) -> Unit
) {
    onError("Exportação em PDF ainda não suportada no Android.")
}
