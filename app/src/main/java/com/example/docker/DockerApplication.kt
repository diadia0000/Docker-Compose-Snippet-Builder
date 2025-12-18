package com.example.docker

import android.app.Application
import androidx.room.Room
import com.example.docker.data.AppDatabase
import com.example.docker.data.TemplateRepository
import com.example.docker.data.LocalUserPreferencesRepository
import com.example.docker.network.SupabaseClient

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

class DockerApplication : Application() {

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE service_templates ADD COLUMN category TEXT NOT NULL DEFAULT 'General'")
        }
    }

    lateinit var database: AppDatabase
    lateinit var repository: TemplateRepository
    lateinit var userPreferencesRepository: LocalUserPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "docker_snippet_db"
        )
        .fallbackToDestructiveMigration()
        .addMigrations(MIGRATION_4_5)
        .build()

        repository = TemplateRepository(database.templateDao(), SupabaseClient.client, applicationContext)
        userPreferencesRepository = LocalUserPreferencesRepository(applicationContext)
    }
}
