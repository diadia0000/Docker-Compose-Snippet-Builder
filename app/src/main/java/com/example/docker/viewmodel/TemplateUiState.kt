package com.example.docker.viewmodel

import com.example.docker.data.ServiceTemplate

data class TemplateUiState(
    val isLoading: Boolean = false,
    val templates: List<ServiceTemplate> = emptyList(),
    val generatedYaml: String = "", // For the detail view
    val connectionStatus: String = "Unknown" // New field for connection status
)
