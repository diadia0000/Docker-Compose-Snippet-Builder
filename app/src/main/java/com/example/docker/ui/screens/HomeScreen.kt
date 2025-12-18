package com.example.docker.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Delete
import com.example.docker.viewmodel.SortOption
import com.example.docker.viewmodel.isSelectionMode
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.docker.data.ServiceTemplate
import com.example.docker.ui.theme.*
import com.example.docker.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToForm: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        if (uiState.isSelectionMode) {
                            Text(
                                "${uiState.selectedIds.size} Selected",
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                            }
                        } else {
                            Text(
                                "🐳 Docker Snippets",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.serviceList.size} templates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.syncWithCloud()
                                Toast.makeText(context, "🔄 Syncing with cloud...", Toast.LENGTH_SHORT).show()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = DockerBlue.copy(alpha = 0.1f),
                                contentColor = DockerBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sync"
                            )
                        }

                        // Sort Menu
                        var showSortMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date (Newest)") },
                                    onClick = { 
                                        viewModel.sort(SortOption.DATE_DESC)
                                        showSortMenu = false 
                                    },
                                    leadingIcon = { 
                                        if(uiState.sortOption == SortOption.DATE_DESC) Icon(Icons.Default.Star, contentDescription = null, tint = DockerBlue)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name (A-Z)") },
                                    onClick = { 
                                        viewModel.sort(SortOption.NAME_ASC)
                                        showSortMenu = false 
                                    },
                                    leadingIcon = { 
                                        if(uiState.sortOption == SortOption.NAME_ASC) Icon(Icons.Default.Star, contentDescription = null, tint = DockerBlue)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Favorites First") },
                                    onClick = { 
                                        viewModel.sort(SortOption.FAVORITE_FIRST)
                                        showSortMenu = false 
                                    },
                                    leadingIcon = { 
                                        if(uiState.sortOption == SortOption.FAVORITE_FIRST) Icon(Icons.Default.Star, contentDescription = null, tint = DockerBlue)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Last Used") },
                                    onClick = { 
                                        viewModel.sort(SortOption.LAST_USED)
                                        showSortMenu = false 
                                    },
                                    leadingIcon = { 
                                        if(uiState.sortOption == SortOption.LAST_USED) Icon(Icons.Default.Star, contentDescription = null, tint = DockerBlue)
                                    }
                                )
                            }
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToForm,
                containerColor = DockerBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Service", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fancy Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    placeholder = {
                        Text(
                            "Search templates...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DockerBlue
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DockerBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Category Filters
            if (uiState.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DockerBlue.copy(alpha = 0.2f),
                            selectedLabelColor = DockerBlue
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedCategory == null,
                            borderColor = if (uiState.selectedCategory == null) DockerBlue else MaterialTheme.colorScheme.outline.copy(alpha=0.3f),
                            selectedBorderColor = DockerBlue
                        )
                    )
                    
                    uiState.categories.forEach { category ->
                         FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(if (uiState.selectedCategory == category) null else category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DockerBlue.copy(alpha = 0.2f),
                                selectedLabelColor = DockerBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedCategory == category,
                                borderColor = if (uiState.selectedCategory == category) DockerBlue else MaterialTheme.colorScheme.outline.copy(alpha=0.3f),
                                selectedBorderColor = DockerBlue
                            )
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = DockerBlue,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading templates...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (uiState.serviceList.isEmpty()) {
                EmptyStateWidget(
                    searchQuery = uiState.searchQuery,
                    onAddClick = onNavigateToForm
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.serviceList) { template ->
                        ServiceTemplateItem(
                            template = template,
                            isSelected = uiState.selectedIds.contains(template.id),
                            isSelectionMode = uiState.isSelectionMode,
                            onClick = { 
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSelection(template.id)
                                } else {
                                    onNavigateToDetail(template.id) 
                                }
                            },
                            onLongClick = { viewModel.toggleSelection(template.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmptyStateWidget(
    searchQuery: String,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DockerBlue.copy(alpha = 0.2f), AccentCyan.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = DockerBlue
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) "No results found" else "No templates yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotEmpty())
                    "Try a different search term"
                else
                    "Create your first Docker service template",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(
                    onClick = onAddClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DockerBlue.copy(alpha = 0.1f),
                        contentColor = DockerBlue
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Template")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceTemplateItem(
    template: ServiceTemplate,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleFavorite: (ServiceTemplate) -> Unit
) {
    val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DockerBlue.copy(alpha = 0.1f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null // Handled by card click
                )
            } else {
                // Docker Icon Container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DockerBlue, AccentCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (template.category != "General" && template.category.isNotBlank()) {
                     SuggestionChip(
                        onClick = { },
                        label = { Text(template.category, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)
                        ),
                        border = null
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = DockerBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = template.image,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = DockerBlue
                    )
                }
                if (template.ports.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentOrange.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "🔌 ${template.ports}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Favorite indicator/toggle
            IconButton(onClick = { onToggleFavorite(template) }) {
                Icon(
                    imageVector = if (template.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (template.isFavorite) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

