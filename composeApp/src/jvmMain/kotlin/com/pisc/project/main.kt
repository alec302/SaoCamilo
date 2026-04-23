package com.pisc.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pisc.project.App // Seu App.kt
import com.pisc.project.data.local.getDatabaseBuilder
import com.pisc.project.data.local.getRoomDatabase

fun main() = application {
    // 1. Constrói o banco apontando para a sua pasta de usuário
    val builder = getDatabaseBuilder()
    val db = getRoomDatabase(builder)
    val dao = db.sweatRateDao()

    // 2. Inicia a Janela do Desktop
    Window(
        onCloseRequest = ::exitApplication,
        title = "São Camilo - Taxa de Sudorese",
    ) {
        // 3. Passa o DAO para a UI!
        App(dao = dao)
    }
}