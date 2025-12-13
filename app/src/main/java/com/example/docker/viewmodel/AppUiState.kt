package com.example.docker.viewmodel

import com.example.docker.data.ServiceTemplate

data class AppUiState(
    val isLoading: Boolean = false,
    val serviceList: List<ServiceTemplate> = emptyList(),
    val currentService: ServiceTemplate? = null, // For Detail/Edit screen
    val error: String? = null
)

