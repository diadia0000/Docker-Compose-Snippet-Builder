package com.example.docker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.docker.data.ServiceTemplate
import com.example.docker.data.TemplateRepository
import com.example.docker.utils.YamlGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val repository: TemplateRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.update { it.copy(serviceList = list) }
            }
        }
    }

    fun selectTemplate(template: ServiceTemplate?) {
        _uiState.update { it.copy(currentService = template) }
    }

    fun saveTemplate(name: String, image: String, ports: String, env: String, volumes: String) {
        val template = ServiceTemplate(
            id = _uiState.value.currentService?.id ?: 0,
            name = name,
            image = image,
            ports = ports,
            environment = env,
            volumes = volumes,
            yaml = "" // Will be generated
        )

        val yaml = YamlGenerator.generateYaml(template)
        val finalTemplate = template.copy(yaml = yaml)

        viewModelScope.launch {
            repository.insertTemplate(finalTemplate)
            // Reset current selection or navigate back logic if needed
            // For now, just clear selection
            selectTemplate(null)
        }
    }

    fun deleteTemplate(template: ServiceTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun uploadToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value.currentService
            if (current != null) {
                repository.uploadToCloud(current)
            } else {
                // Upload all? Or handle error?
                // For now, let's upload all
                _uiState.value.serviceList.forEach {
                    repository.uploadToCloud(it)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun downloadFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.downloadFromCloud()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

class AppViewModelFactory(private val repository: TemplateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

