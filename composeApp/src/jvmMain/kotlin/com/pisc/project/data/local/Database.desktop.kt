package com.pisc.project.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), "sweat_rate_desktop.db")

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
        // A linha "factory = ..." foi apagada. O Room resolve sozinho agora!
    )
}