package com.pisc.project.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [SweatRateEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class) // <- 1. Avisa o Room para criar o construtor
abstract class AppDatabase : RoomDatabase() {
    abstract fun sweatRateDao(): SweatRateDao
}

// 2. Criamos a "promessa" (expect). O KSP do Room vai gerar a implementação real (actual) por debaixo dos panos!
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>