package com.example.docker.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class TemplateRepository(
    private val templateDao: TemplateDao,
    private val supabaseClient: SupabaseClient
) {
    val allTemplates: Flow<List<ServiceTemplate>> = templateDao.getAllTemplates()

    fun getTemplate(id: Int): Flow<ServiceTemplate> = templateDao.getTemplate(id)

    suspend fun insertTemplate(template: ServiceTemplate) {
        templateDao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: ServiceTemplate) {
        templateDao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: ServiceTemplate) {
        templateDao.deleteTemplate(template)
    }

    // Supabase Integration (Day 4)

    suspend fun uploadToCloud(template: ServiceTemplate) {
        try {
            supabaseClient.postgrest["service_templates"].upsert(template)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }

    suspend fun downloadFromCloud(): List<ServiceTemplate> {
        return try {
            val result = supabaseClient.postgrest["service_templates"]
                .select()
                .decodeList<ServiceTemplate>()

            // Save to local DB
            result.forEach { templateDao.insertTemplate(it) }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

