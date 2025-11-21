package com.example.myapplication.NavController

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Screens.LoginScreen
import com.example.myapplication.Screens.PrincipalScreen
import com.example.myapplication.Screens.ProgramarScreen
import com.example.myapplication.Screens.SonidosScreen
import com.example.myapplication.Screens.EstadoHogarScreen
import com.example.myapplication.Screens.NotificacionesScreen

@Composable
fun NavController() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("principal") { PrincipalScreen(navController) }
        composable("programar") { ProgramarScreen(navController) }
        composable("sonidos") { SonidosScreen(navController) }
        composable("Estado Hogar") { EstadoHogarScreen(navController) }
        composable("Notificaciones") { NotificacionesScreen(navController) }
    }
}
