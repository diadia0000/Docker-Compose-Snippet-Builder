package com.example.docker

import android.app.Application
import androidx.room.Room
import com.example.docker.data.AppDatabase
import com.example.docker.data.TemplateRepository
import com.example.docker.network.SupabaseClient

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DockerApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: TemplateRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "docker_snippet_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-fill with default data
                val now = System.currentTimeMillis()

                // Nginx
                db.execSQL("INSERT INTO service_templates (name, image, ports, volumes, env_vars, restart_policy, created_at) VALUES ('nginx', 'nginx:latest', '80:80', '', '{}', 'always', $now)")

                // Redis
                db.execSQL("INSERT INTO service_templates (name, image, ports, volumes, env_vars, restart_policy, created_at) VALUES ('redis', 'redis:alpine', '6379:6379', '', '{}', 'always', $now)")

                // Postgres
                // Note: env_vars is a JSON string
                db.execSQL("INSERT INTO service_templates (name, image, ports, volumes, env_vars, restart_policy, created_at) VALUES ('postgres', 'postgres:13', '5432:5432', 'pgdata:/var/lib/postgresql/data', '{\"POSTGRES_PASSWORD\":\"example\"}', 'always', $now)")
            }
        })
        .build()

        repository = TemplateRepository(database.templateDao(), SupabaseClient.client, applicationContext)
    }
}
