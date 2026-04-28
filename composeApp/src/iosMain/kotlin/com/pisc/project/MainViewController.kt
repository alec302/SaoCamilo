package com.pisc.project

import androidx.compose.ui.window.ComposeUIViewController
import com.pisc.project.data.local.getDatabaseBuilder
import com.pisc.project.data.local.getRoomDatabase
import com.pisc.project.data.remote.CloudSweatRateDataSource
import com.pisc.project.data.repository.SweatRateRepository

fun MainViewController() = ComposeUIViewController {
    val builder = getDatabaseBuilder()
    val db = getRoomDatabase(builder)
    val dao = db.sweatRateDao()
    val cloudDataSource = CloudSweatRateDataSource()
    val repository = SweatRateRepository(dao, cloudDataSource)

    App(repository = repository)
}