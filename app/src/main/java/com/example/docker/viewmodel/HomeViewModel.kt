package com.example.docker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docker.DockerApplication
import com.example.docker.data.ServiceTemplate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val serviceList: List<ServiceTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as DockerApplication).repository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.allTemplates.collect { templates ->
                _uiState.update { it.copy(serviceList = templates, isLoading = false) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                repository.allTemplates.collect { templates ->
                    _uiState.update { it.copy(serviceList = templates) }
                }
            } else {
                repository.searchTemplates(query).collect { templates ->
                    _uiState.update { it.copy(serviceList = templates) }
                }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.allTemplates.collect { templates ->
                _uiState.update { it.copy(serviceList = templates) }
            }
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.downloadTemplates()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

