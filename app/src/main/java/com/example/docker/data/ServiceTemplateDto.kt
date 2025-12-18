package com.example.docker.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Supabase synchronization
 * Only includes fields that exist in the Supabase database schema
 * Note: Supabase uses BIGSERIAL (Long) for id, but local Room uses Int
 * All fields use nullable types with defaults to handle null values from Supabase
 */
@Serializable
data class ServiceTemplateDto(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
    val ports: String? = null,
    val volumes: String? = null,
    @SerialName("env_vars")
    val envVars: String? = null,
    @SerialName("restart_policy")
    val restartPolicy: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("created_at")
    val createdAt: Long? = null
)

/**
 * Extension functions to convert between ServiceTemplate and ServiceTemplateDto
 */
fun ServiceTemplate.toDto(): ServiceTemplateDto {
    return ServiceTemplateDto(
        id = null, // Don't send local Room ID to Supabase - let Supabase manage its own IDs
        name = name,
        image = image,
        ports = ports,
        volumes = volumes,
        envVars = envVars,
        restartPolicy = restartPolicy,
        category = category,
        createdAt = createdAt
    )
}

fun ServiceTemplateDto.toServiceTemplate(
    isFavorite: Boolean = false,
    lastUsed: Long = 0L
): ServiceTemplate {
    return ServiceTemplate(
        id = (id ?: 0L).toInt(),
        name = name ?: "",
        image = image ?: "",
        ports = ports ?: "",
        volumes = volumes ?: "",
        envVars = envVars ?: "",
        restartPolicy = restartPolicy ?: "no",
        category = category ?: "General",
        createdAt = createdAt ?: System.currentTimeMillis(),
        isFavorite = isFavorite,
        lastUsed = lastUsed
    )
}
