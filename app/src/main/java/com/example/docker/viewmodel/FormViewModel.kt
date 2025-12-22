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
import org.json.JSONObject

data class EnvVarItem(val key: String, val value: String)

data class FormUiState(
    val name: String = "",
    val image: String = "",
    val ports: String = "",
    val volumes: String = "",
    val envVars: String = "",
    val restartPolicy: String = "no",
    val category: String = "General",
    val envVarList: List<EnvVarItem> = emptyList(),
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

    // Store original template data to preserve fields like createdAt, isFavorite, lastUsed when editing
    private var originalTemplate: ServiceTemplate? = null

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
                originalTemplate = template  // Save original for preserving fields during update
                _uiState.update {
                    it.copy(
                        name = template.name,
                        image = template.image,
                        ports = template.ports,
                        volumes = template.volumes,
                        envVars = template.envVars,
                        envVarList = parseEnvVars(template.envVars),
                        restartPolicy = template.restartPolicy,
                        category = template.category,
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
    fun updateCategory(value: String) { _uiState.update { it.copy(category = value) } }

    fun addEnvVar() {
        _uiState.update { it.copy(envVarList = it.envVarList + EnvVarItem("", "")) }
    }

    fun updateEnvVar(index: Int, key: String, value: String) {
        _uiState.update { 
            val newList = it.envVarList.toMutableList()
            if (index in newList.indices) {
                newList[index] = EnvVarItem(key, value)
                it.copy(envVarList = newList)
            } else it
        }
    }

    fun removeEnvVar(index: Int) {
        _uiState.update { 
            val newList = it.envVarList.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
                it.copy(envVarList = newList)
            } else it
        }
    }

    private fun parseEnvVars(jsonString: String): List<EnvVarItem> {
        return try {
            val json = JSONObject(if (jsonString.isBlank()) "{}" else jsonString)
            val list = mutableListOf<EnvVarItem>()
            json.keys().forEach { key ->
                list.add(EnvVarItem(key, json.getString(key)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeEnvVars(list: List<EnvVarItem>): String {
        val json = JSONObject()
        list.forEach { 
            if (it.key.isNotBlank()) {
                json.put(it.key, it.value) 
            }
        }
        return json.toString()
    }

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
                envVars = serializeEnvVars(state.envVarList),
                restartPolicy = state.restartPolicy,
                category = state.category,
                // Preserve original values when editing, use defaults for new template
                createdAt = originalTemplate?.createdAt ?: System.currentTimeMillis(),
                isFavorite = originalTemplate?.isFavorite ?: false,
                lastUsed = originalTemplate?.lastUsed ?: 0L
            )
            repository.insertTemplate(template)
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}

