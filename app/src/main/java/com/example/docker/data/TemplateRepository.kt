package com.example.docker.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class TemplateRepository(
    private val templateDao: TemplateDao,
    private val supabaseClient: SupabaseClient
) {
    val allTemplates: Flow<List<ServiceTemplate>> = templateDao.getAllTemplates()

    suspend fun insertTemplate(template: ServiceTemplate) {
        templateDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(template: ServiceTemplate) {
        templateDao.deleteTemplate(template)
    }

    // Supabase Integration
    suspend fun uploadTemplates(templates: List<ServiceTemplate>) {
        try {
            if (templates.isNotEmpty()) {
                supabaseClient.postgrest["service_templates"].upsert(templates)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Rethrow or handle as needed by ViewModel
        }
    }

    suspend fun downloadTemplates(): List<ServiceTemplate> {
        return try {
            val result = supabaseClient.postgrest["service_templates"]
                .select()
                .decodeList<ServiceTemplate>()

            // Sync logic: Overwrite or Merge?
            // Simple strategy: Insert/Update local DB with remote data
            result.forEach { templateDao.insertTemplate(it) }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

