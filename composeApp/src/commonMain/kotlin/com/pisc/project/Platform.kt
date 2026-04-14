package com.pisc.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform