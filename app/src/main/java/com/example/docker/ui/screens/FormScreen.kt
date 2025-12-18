package com.example.docker.ui.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docker.ui.theme.*
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
    val isEditing = templateId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isEditing) "✏️ Edit Service" else "✨ New Service",
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.name.isNotEmpty()) {
                            Text(
                                uiState.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DockerBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Header Card with gradient
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DockerBlue, AccentCyan)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "🐳 Service Configuration",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Configure your Docker service template",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Form Fields
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FancyTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = "Service Name",
                        placeholder = "e.g., my-nginx-server",
                        icon = Icons.AutoMirrored.Outlined.Label,
                        iconTint = DockerBlue
                    )

                    Column {
                        FancyTextField(
                            value = uiState.category,
                            onValueChange = viewModel::updateCategory,
                            label = "Category",
                            placeholder = "e.g., Database, Web Server",
                            icon = Icons.Outlined.Bookmarks,
                            iconTint = AccentPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp).horizontalScroll(rememberScrollState())
                        ) {
                            listOf("Web Server", "Database", "Cache", "Message Queue").forEach { cat ->
                                SuggestionChip(
                                    onClick = { viewModel.updateCategory(cat) },
                                    label = { Text(cat) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if(uiState.category == cat) DockerBlue.copy(alpha=0.1f) else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    FancyTextField(
                        value = uiState.image,
                        onValueChange = viewModel::updateImage,
                        label = "Docker Image",
                        placeholder = "e.g., nginx:latest",
                        icon = Icons.Outlined.Storage,
                        iconTint = AccentCyan
                    )

                    FancyTextField(
                        value = uiState.ports,
                        onValueChange = viewModel::updatePorts,
                        label = "Port Mapping",
                        placeholder = "e.g., 8080:80",
                        icon = Icons.Outlined.SettingsEthernet,
                        iconTint = AccentOrange
                    )

                    FancyTextField(
                        value = uiState.volumes,
                        onValueChange = viewModel::updateVolumes,
                        label = "Volume Mapping",
                        placeholder = "e.g., ./data:/data",
                        icon = Icons.Outlined.Folder,
                        iconTint = AccentPurple
                    )

                    // Environment Variables (Key-Value UI)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Code, contentDescription = null, tint = StatusSuccess)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Environment Variables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (uiState.envVarList.isEmpty()) {
                                Text("No environment variables defined", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            uiState.envVarList.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.key,
                                        onValueChange = { viewModel.updateEnvVar(index, it, item.value) },
                                        placeholder = { Text("Key") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Text("=", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    OutlinedTextField(
                                        value = item.value,
                                        onValueChange = { viewModel.updateEnvVar(index, item.key, it) },
                                        placeholder = { Text("Value") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    IconButton(onClick = { viewModel.removeEnvVar(index) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = { viewModel.addEnvVar() },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, DockerBlue),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DockerBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Variable")
                            }
                        }
                    }

                    // Restart Policy Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = DockerBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Restart Policy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("no", "always", "on-failure").forEach { policy ->
                                    val isSelected = uiState.restartPolicy == policy
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateRestartPolicy(policy) },
                                        label = { Text(policy) },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DockerBlue,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Button(
                        onClick = { viewModel.save(onSuccess = onNavigateBack) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DockerBlue
                        )
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditing) "Update Service" else "Create Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun FancyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    iconTint: Color,
    minLines: Int = 1
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = iconTint,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            minLines = minLines
        )
    }
}

