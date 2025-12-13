package com.example.docker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.docker.data.ServiceTemplate
import com.example.docker.viewmodel.TemplateViewModel

@Composable
fun TestScreen(viewModel: TemplateViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Docker Compose Snippet Builder Test", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                val newTemplate = ServiceTemplate(
                    name = "web-${System.currentTimeMillis() % 1000}",
                    image = "nginx:latest",
                    ports = "80:80",
                    restartPolicy = "always"
                )
                viewModel.saveTemplate(newTemplate)
            }) {
                Text("Add Mock Data")
            }

            Button(onClick = { viewModel.syncWithCloud() }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Sync Cloud")
            }

            Button(onClick = { viewModel.checkConnection() }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Check Conn")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        Text(
            text = "Status: ${uiState.connectionStatus}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (uiState.generatedYaml.isNotEmpty()) {
            Text("Generated YAML:", style = MaterialTheme.typography.labelLarge)
            Text(uiState.generatedYaml, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(uiState.templates) { template ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("ID: ${template.id} | Name: ${template.name}")
                        Text("Image: ${template.image}")
                        Button(onClick = { viewModel.generateYaml(template) }) {
                            Text("Generate YAML")
                        }
                        Button(onClick = { viewModel.deleteTemplate(template) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
