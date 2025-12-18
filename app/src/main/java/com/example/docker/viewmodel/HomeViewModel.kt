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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SortOption {
    NAME_ASC, DATE_DESC, FAVORITE_FIRST, LAST_USED
}

data class HomeUiState(
    val serviceList: List<ServiceTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_DESC,
    val selectedIds: Set<Int> = emptySet(),

    val userMessage: String? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList()
)
val HomeUiState.isSelectionMode: Boolean get() = selectedIds.isNotEmpty()

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
                _uiState.update { currentState ->
                    processList(templates, currentState)
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                repository.allTemplates.collect { templates ->
                    _uiState.update { currentState ->
                        currentState.copy(serviceList = sortTemplates(templates, currentState.sortOption))
                    }
                }
            } else {
                repository.searchTemplates(query).collect { templates ->
                    _uiState.update { currentState ->
                        currentState.copy(serviceList = sortTemplates(templates, currentState.sortOption))
                    }
                }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.allTemplates.collect { templates ->
                _uiState.update { currentState ->

                    processList(templates, currentState)
                }
            }
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Upload local templates first to ensure nothing is lost
                val localTemplates = repository.allTemplates.first()
                repository.uploadTemplates(localTemplates)
                
                // 2. Then download (this will replace local with cloud, but cloud now has our data)
                repository.downloadTemplates()
                _uiState.update { it.copy(userMessage = "✅ Sync completed!") }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(userMessage = "❌ Sync failed: ${e.message}") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun toggleFavorite(template: ServiceTemplate) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(template.id, !template.isFavorite)
        }
    }


 
    private var currentTemplates: List<ServiceTemplate> = emptyList()

    fun selectCategory(category: String?) {
        _uiState.update { 
            val newState = it.copy(selectedCategory = category)
            processList(currentTemplates, newState)
        }
    }

    private fun processList(templates: List<ServiceTemplate>, state: HomeUiState): HomeUiState {
        currentTemplates = templates // Cache it
        val categories = templates.map { it.category }.distinct().sorted()
        
        var filtered = templates
        if (state.selectedCategory != null) {
            filtered = filtered.filter { it.category == state.selectedCategory }
        }
        
        val sorted = sortTemplates(filtered, state.sortOption)
        
        return state.copy(
            serviceList = sorted,
            categories = categories,
            isLoading = false
        )
    }

    fun sort(option: SortOption) {
        _uiState.update { currentState ->
             val newState = currentState.copy(sortOption = option)
             processList(currentTemplates, newState)
        }
    }

    private fun sortTemplates(list: List<ServiceTemplate>, option: SortOption): List<ServiceTemplate> {
        return when (option) {
            SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            SortOption.DATE_DESC -> list.sortedByDescending { it.createdAt }
            SortOption.FAVORITE_FIRST -> list.sortedWith(compareByDescending<ServiceTemplate> { it.isFavorite }.thenByDescending { it.createdAt })
            SortOption.LAST_USED -> list.sortedByDescending { it.lastUsed }
        }
    }

    fun toggleSelection(id: Int) {
        _uiState.update { currentState ->
            val newSelection = if (currentState.selectedIds.contains(id)) {
                currentState.selectedIds - id
            } else {
                currentState.selectedIds + id
            }
            currentState.copy(selectedIds = newSelection)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val idsToDelete = _uiState.value.selectedIds
            // Assuming repository has delete functionality, but usually takes object.
            // We can iterate or add bulk delete. For simple difficulty, iterate is fine.
            // Or better, filtered list from current state.
            val templatesToDelete = _uiState.value.serviceList.filter { it.id in idsToDelete }
            
            // Optimistic update or just wait for flow? Flow is better.
            // But we need to call delete on repo.
            templatesToDelete.forEach { repository.deleteTemplate(it) }
            
            clearSelection()
        }
    }
}

