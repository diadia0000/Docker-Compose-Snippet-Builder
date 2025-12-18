package com.example.docker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "service_templates")
@Serializable
data class ServiceTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val image: String = "",
    val ports: String = "", // e.g., "8080:80"
    val volumes: String = "", // e.g., "/host:/container"
    @ColumnInfo(name = "env_vars")
    @SerialName("env_vars")
    val envVars: String = "", // JSON format for environment variables
    @ColumnInfo(name = "restart_policy")
    @SerialName("restart_policy")
    val restartPolicy: String = "no",
    @ColumnInfo(name = "category")
    @SerialName("category")
    val category: String = "General",
    @ColumnInfo(name = "created_at")
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_favorite")
    @Transient
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_used")
    @Transient
    val lastUsed: Long = 0L
)

