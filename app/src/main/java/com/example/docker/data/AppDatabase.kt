package com.example.docker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ServiceTemplate::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao
}

