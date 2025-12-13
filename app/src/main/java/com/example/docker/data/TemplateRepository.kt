package com.example.docker.data

import android.content.Context
import android.util.Log
import com.example.docker.utils.NetworkUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

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
                supabaseClient.postgrest["service_templates"].upsert(template)
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
                supabaseClient.postgrest["service_templates"].upsert(templates)
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
            val result = supabaseClient.postgrest["service_templates"]
                .select()
                .decodeList<ServiceTemplate>()

            // Sync logic: Clear local and replace with remote data
            // This ensures no duplicates
            templateDao.deleteAll()
            templateDao.insertAll(result)
            Log.d(TAG, "Downloaded and synced ${result.size} templates from Supabase")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download templates: ${e.message}")
            throw e
        }
    }
}

