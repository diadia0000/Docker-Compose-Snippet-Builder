package com.example.docker.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docker.DockerApplication
import com.example.docker.viewmodel.FormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    templateId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: FormViewModel = viewModel {
        val application = (this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
        FormViewModel(application, createSavedStateHandle())
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateId == null) "New Service" else "Edit Service") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.image,
                    onValueChange = viewModel::updateImage,
                    label = { Text("Image (e.g., nginx:latest)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.ports,
                    onValueChange = viewModel::updatePorts,
                    label = { Text("Ports (e.g., 8080:80)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.volumes,
                    onValueChange = viewModel::updateVolumes,
                    label = { Text("Volumes (e.g., ./data:/data)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.envVars,
                    onValueChange = viewModel::updateEnvVars,
                    label = { Text("Env Vars (JSON)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Restart Policy", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("no", "always", "on-failure").forEach { policy ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.restartPolicy == policy,
                                onClick = { viewModel.updateRestartPolicy(policy) }
                            )
                            Text(policy)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Button(
                    onClick = { viewModel.save(onSuccess = onNavigateBack) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

