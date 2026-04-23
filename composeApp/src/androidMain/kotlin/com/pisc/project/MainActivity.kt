package com.pisc.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // O import que estava faltando
import com.pisc.project.data.local.getDatabaseBuilder
import com.pisc.project.data.local.getRoomDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Agora ele sabe o que é isso

        val builder = getDatabaseBuilder(applicationContext)
        val db = getRoomDatabase(builder)
        val dao = db.sweatRateDao()

        setContent {
            App(dao = dao)
        }
    }
}