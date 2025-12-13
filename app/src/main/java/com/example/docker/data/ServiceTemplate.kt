package com.example.docker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "service_templates")
@Serializable
data class ServiceTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val image: String,
    val ports: String = "", // e.g., "8080:80"
    val environment: String = "", // e.g., "KEY=VALUE"
    val volumes: String = "", // e.g., "/host:/container"
    val yaml: String = "" // Generated YAML
)

