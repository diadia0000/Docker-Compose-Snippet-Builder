package com.example.docker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.docker.ui.screens.DetailScreen
import com.example.docker.ui.screens.FormScreen
import com.example.docker.ui.screens.HomeScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToForm = { navController.navigate("form") },
                onNavigateToDetail = { id -> navController.navigate("detail/$id") }
            )
        }

        composable(
            route = "detail/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.IntType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getInt("templateId") ?: 0
            DetailScreen(
                templateId = templateId,
                onNavigateToEdit = { id -> navController.navigate("form?templateId=$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "form?templateId={templateId}",
            arguments = listOf(
                navArgument("templateId") {
                    type = NavType.IntType
                    defaultValue = -1 // Use -1 or null logic. NavType.IntType doesn't support null directly easily without nullable=true and defaultValue
                }
            )
        ) { backStackEntry ->
            // If templateId is not provided in URL, it uses defaultValue.
            // However, we want nullable behavior.
            // Let's use defaultValue = -1 to indicate "create mode" if we want to stick to IntType.
            // Or we can use nullable = true but then we need to handle it.
            // Let's stick to defaultValue = -1 for simplicity, assuming valid IDs are > 0.
            val templateId = backStackEntry.arguments?.getInt("templateId") ?: -1
            val finalId = if (templateId == -1) null else templateId

            FormScreen(
                templateId = finalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

