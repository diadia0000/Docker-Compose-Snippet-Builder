package com.example.docker.ui.screens

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docker.utils.YamlGenerator
import com.example.docker.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    templateId: Int,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = viewModel {
        val application = (this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
        DetailViewModel(application, createSavedStateHandle())
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.template?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.delete(onSuccess = onNavigateBack) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(templateId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading || uiState.template == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val template = uiState.template!!
            val yaml = YamlGenerator.toYaml(template)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Section
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image: ${template.image}", style = MaterialTheme.typography.bodyLarge)
                        if (template.ports.isNotBlank()) {
                            Text("Ports: ${template.ports}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (template.volumes.isNotBlank()) {
                            Text("Volumes: ${template.volumes}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Restart: ${template.restartPolicy}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // YAML Preview Section
                Text("Generated YAML", style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B))
                ) {
                    Text(
                        text = yaml,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Button(
                    onClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("YAML", yaml)
                        clipboardManager.setPrimaryClip(clip)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copy YAML")
                }
            }
        }
    }
}

