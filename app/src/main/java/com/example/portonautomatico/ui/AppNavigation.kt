package com.example.portonautomatico.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.portonautomatico.ui.screens.AccessScreen
import com.example.portonautomatico.ui.screens.CrearPortonScreen
import com.example.portonautomatico.ui.screens.HomeScreen
import com.example.portonautomatico.ui.screens.LoginScreen
import com.example.portonautomatico.ui.screens.RegisterScreen
import com.example.portonautomatico.ui.screens.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("access") { AccessScreen(navController) }
        composable("crearPorton") { CrearPortonScreen(navController) }

        // Ruta con parámetro: home/{portonId}
        composable(
            "home/{portonId}",
            arguments = listOf(navArgument("portonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val portonId = backStackEntry.arguments?.getString("portonId") ?: ""
            HomeScreen(navController = navController, portonId = portonId)
        }
    }
}