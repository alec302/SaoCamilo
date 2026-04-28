package com.pisc.project.util

import com.pisc.project.data.local.SweatRateEntity

expect fun exportHistoryToPdf(
    history: List<SweatRateEntity>, 
    userEmail: String, 
    onSuccess: (String) -> Unit, 
    onError: (String) -> Unit
)
