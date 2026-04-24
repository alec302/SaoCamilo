package com.pisc.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
// No commonMain/.../Platform.kt
expect fun getEpochTime(): Long