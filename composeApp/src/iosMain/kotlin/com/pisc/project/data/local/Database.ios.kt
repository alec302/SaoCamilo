package com.pisc.project.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/sweat_rate_ios.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
