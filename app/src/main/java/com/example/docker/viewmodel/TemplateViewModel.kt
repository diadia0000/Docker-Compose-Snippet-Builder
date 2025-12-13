package com.example.docker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.docker.data.ServiceTemplate
import com.example.docker.data.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TemplateViewModel(private val repository: TemplateRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.update { it.copy(templates = list) }
            }
        }
    }

    fun saveTemplate(template: ServiceTemplate) {
        viewModelScope.launch {
            repository.insertTemplate(template)
        }
    }

    fun deleteTemplate(template: ServiceTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, connectionStatus = "Syncing...") }
            try {
                // 1. Upload local templates
                val currentTemplates = _uiState.value.templates
                repository.uploadTemplates(currentTemplates)

                // 2. Download remote templates
                repository.downloadTemplates()

                _uiState.update { it.copy(connectionStatus = "Success: Synced with Supabase") }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(connectionStatus = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, connectionStatus = "Checking connection...") }
            try {
                // Try to fetch just one item or count to verify connection
                repository.downloadTemplates() // Reusing download for now as a check
                _uiState.update { it.copy(connectionStatus = "Connected to Supabase") }
            } catch (e: Exception) {
                _uiState.update { it.copy(connectionStatus = "Connection Failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun generateYaml(template: ServiceTemplate) {
        val yaml = buildString {
            append("version: '3.8'\n")
            append("services:\n")
            append("  ${template.name}:\n")
            append("    image: ${template.image}\n")

            if (template.ports.isNotBlank()) {
                append("    ports:\n")
                template.ports.split(",").forEach { port ->
                    if (port.isNotBlank()) {
                        append("      - \"${port.trim()}\"\n")
                    }
                }
            }

            if (template.volumes.isNotBlank()) {
                append("    volumes:\n")
                template.volumes.split(",").forEach { vol ->
                    if (vol.isNotBlank()) {
                        append("      - ${vol.trim()}\n")
                    }
                }
            }

            append("    restart: ${template.restartPolicy}\n")

            if (template.envVars.isNotBlank() && template.envVars != "{}") {
                try {
                    val jsonElement = Json.parseToJsonElement(template.envVars)
                    val jsonObject = jsonElement.jsonObject
                    if (jsonObject.isNotEmpty()) {
                        append("    environment:\n")
                        jsonObject.forEach { (key, value) ->
                            append("      - $key=${value.jsonPrimitive.content}\n")
                        }
                    }
                } catch (e: Exception) {
                    append("    # Error parsing env_vars JSON: ${e.message}\n")
                }
            }
        }
        _uiState.update { it.copy(generatedYaml = yaml) }
    }
}

class TemplateViewModelFactory(private val repository: TemplateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TemplateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TemplateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
