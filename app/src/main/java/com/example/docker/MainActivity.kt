package com.example.docker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.docker.ui.theme.DockerTheme
import androidx.lifecycle.ViewModelProvider
import com.example.docker.ui.screens.TestScreen
import com.example.docker.viewmodel.TemplateViewModel
import com.example.docker.viewmodel.TemplateViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DockerApplication
        val repository = app.repository

        // Manually create ViewModel using ViewModelProvider
        val factory = TemplateViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[TemplateViewModel::class.java]

        setContent {
            DockerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Apply padding to avoid overlapping with system bars
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        TestScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}