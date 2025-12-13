package com.example.docker

import android.app.Application
import androidx.room.Room
import com.example.docker.data.AppDatabase
import com.example.docker.data.TemplateRepository
import com.example.docker.network.SupabaseClient

class DockerApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: TemplateRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "docker_snippet_db"
        ).build()

        repository = TemplateRepository(database.templateDao(), SupabaseClient.client)
    }
}

