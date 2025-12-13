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

data class FormUiState(
    val name: String = "",
    val image: String = "",
    val ports: String = "",
    val volumes: String = "",
    val envVars: String = "",
    val restartPolicy: String = "no",
    val isLoading: Boolean = false
)

class FormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as DockerApplication).repository
    private val dao = (application as DockerApplication).database.templateDao()
    private val templateId: Int? = savedStateHandle.get<Int>("templateId")?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        if (templateId != null) {
            loadTemplate(templateId)
        }
    }

    private fun loadTemplate(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val template = dao.getTemplate(id).firstOrNull()
            if (template != null) {
                _uiState.update {
                    it.copy(
                        name = template.name,
                        image = template.image,
                        ports = template.ports,
                        volumes = template.volumes,
                        envVars = template.envVars,
                        restartPolicy = template.restartPolicy,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(value: String) { _uiState.update { it.copy(name = value) } }
    fun updateImage(value: String) { _uiState.update { it.copy(image = value) } }
    fun updatePorts(value: String) { _uiState.update { it.copy(ports = value) } }
    fun updateVolumes(value: String) { _uiState.update { it.copy(volumes = value) } }
    fun updateEnvVars(value: String) { _uiState.update { it.copy(envVars = value) } }
    fun updateRestartPolicy(value: String) { _uiState.update { it.copy(restartPolicy = value) } }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val template = ServiceTemplate(
                id = templateId ?: 0,
                name = state.name,
                image = state.image,
                ports = state.ports,
                volumes = state.volumes,
                envVars = state.envVars,
                restartPolicy = state.restartPolicy,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTemplate(template)
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}

