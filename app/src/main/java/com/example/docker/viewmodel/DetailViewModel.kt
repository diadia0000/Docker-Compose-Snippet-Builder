package com.example.docker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.docker.DockerApplication
import com.example.docker.data.ServiceTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val template: ServiceTemplate? = null,
    val isLoading: Boolean = false
)

class DetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as DockerApplication).repository
    private val dao = (application as DockerApplication).database.templateDao()
    private val templateId: Int = checkNotNull(savedStateHandle["templateId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadTemplate()
    }

    private fun loadTemplate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val template = dao.getTemplate(templateId).firstOrNull()
            _uiState.update { it.copy(template = template, isLoading = false) }
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val template = _uiState.value.template
            if (template != null) {
                repository.deleteTemplate(template)
            }
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}

