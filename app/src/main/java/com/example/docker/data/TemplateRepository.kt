package com.example.docker.data

import android.content.Context
import android.util.Log
import com.example.docker.utils.NetworkUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TemplateRepository(
    private val templateDao: TemplateDao,
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    companion object {
        private const val TAG = "TemplateRepository"
    }

    val allTemplates: Flow<List<ServiceTemplate>> = templateDao.getAllTemplates()

    fun searchTemplates(query: String): Flow<List<ServiceTemplate>> {
        return templateDao.searchTemplates(query)
    }

    suspend fun insertTemplate(template: ServiceTemplate) {
        // 1. Save to local Room Database first
        templateDao.insertTemplate(template)

        // 2. Try to sync to Supabase if network available
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                // Use DTO to exclude local-only fields
                // onConflict = "name" ensures upsert uses name field to detect duplicates
                supabaseClient.postgrest["service_templates"].upsert(template.toDto()) {
                    onConflict = "name"
                }
                Log.d(TAG, "Template synced to Supabase: ${template.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync template to Supabase: ${e.message}")
                // Don't throw - local save succeeded, cloud sync can retry later
            }
        } else {
            Log.d(TAG, "No network - template saved locally only")
        }
    }

    suspend fun deleteTemplate(template: ServiceTemplate) {
        // 1. Delete from local Room Database
        templateDao.deleteTemplate(template)

        // 2. Try to delete from Supabase if network available
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                supabaseClient.postgrest["service_templates"]
                    .delete { filter { eq("id", template.id) } }
                Log.d(TAG, "Template deleted from Supabase: ${template.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete template from Supabase: ${e.message}")
            }
        }
    }

    // Supabase Integration - Upload all local templates
    suspend fun uploadTemplates(templates: List<ServiceTemplate>) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw Exception("No network connection available")
        }
        try {
            if (templates.isNotEmpty()) {
                // Convert to DTO to exclude local-only fields
                val dtos = templates.map { it.toDto() }
                // onConflict = "name" ensures upsert uses name field to detect duplicates
                supabaseClient.postgrest["service_templates"].upsert(dtos) {
                    onConflict = "name"
                }
                Log.d(TAG, "Uploaded ${templates.size} templates to Supabase")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload templates: ${e.message}")
            throw e
        }
    }

    suspend fun downloadTemplates(): List<ServiceTemplate> {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw Exception("No network connection available")
        }
        return try {
            // Download DTOs from Supabase
            val dtos = supabaseClient.postgrest["service_templates"]
                .select()
                .decodeList<ServiceTemplateDto>()

            Log.d(TAG, "Downloaded ${dtos.size} templates from Supabase")

            // Preserve local-only flags (isFavorite, lastUsed)
            // We match by name since IDs might differ or be overwritten
            val localTemplates = templateDao.getAllTemplates().first()
            val localStateMap = localTemplates.associateBy({ it.name }, { Pair(it.isFavorite, it.lastUsed) })

            // Convert DTOs to ServiceTemplate with preserved local state
            // IMPORTANT: Set id to 0 to let Room auto-generate new local IDs
            val mergedResult = dtos.map { dto ->
                val localState = localStateMap[dto.name]
                dto.toServiceTemplate(
                    isFavorite = localState?.first ?: false,
                    lastUsed = localState?.second ?: 0L
                )
            }

            // Sync logic: Clear local and replace with remote data
            // This ensures no duplicates
            templateDao.deleteAll()
            templateDao.insertAll(mergedResult)
            Log.d(TAG, "Downloaded and synced ${mergedResult.size} templates from Supabase")
            mergedResult
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download templates: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        templateDao.updateFavoriteStatus(id, isFavorite)
        // Note: We might want to sync this status to cloud too, but for simplicity let's keep it local first, 
        // or upsert the whole object if we had it. Since this is just a flag, local is fine for "Simple Difficulty".
    }

    suspend fun updateLastUsed(id: Int) {
        templateDao.updateLastUsed(id, System.currentTimeMillis())
    }
}

