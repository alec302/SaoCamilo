package com.pisc.project.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers // APENAS este import de coroutines

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true) // Limpa o banco velho se a versão mudar (Ideal para dev)
        .setDriver(BundledSQLiteDriver()) // Usa o SQLite embutido que configuramos no Gradle
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}