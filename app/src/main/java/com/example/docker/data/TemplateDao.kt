package com.example.docker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM service_templates ORDER BY id DESC")
    fun getAllTemplates(): Flow<List<ServiceTemplate>>

    @Query("SELECT * FROM service_templates WHERE id = :id")
    fun getTemplate(id: Int): Flow<ServiceTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ServiceTemplate)

    @Update
    suspend fun updateTemplate(template: ServiceTemplate)

    @Delete
    suspend fun deleteTemplate(template: ServiceTemplate)
}

