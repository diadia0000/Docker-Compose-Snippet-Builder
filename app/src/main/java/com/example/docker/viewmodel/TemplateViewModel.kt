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
import com.example.docker.utils.YamlGenerator

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
        val yaml = YamlGenerator.generateCombinedYaml(listOf(template))
        _uiState.update { it.copy(generatedYaml = yaml) }
    }

    fun generateCombinedYaml(templates: List<ServiceTemplate>) {
        val yaml = YamlGenerator.generateCombinedYaml(templates)
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
