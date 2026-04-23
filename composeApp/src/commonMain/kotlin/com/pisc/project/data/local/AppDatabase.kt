package com.pisc.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// É aqui que definimos qual tabela (Entity) o banco vai ter
@Database(entities = [SweatRateEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sweatRateDao(): SweatRateDao
}