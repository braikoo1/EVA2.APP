package com.example.myapplication.NavController

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Screens.LoginScreen
import com.example.myapplication.Screens.PrincipalScreen
import com.example.myapplication.Screens.ProgramarScreen
import com.example.myapplication.Screens.SonidosScreen
import com.example.myapplication.Screens.NotificacionesScreen
import androidx.compose.ui.Modifier
import com.example.myapplication.Composables.NotificacionEnPantalla

@Composable
fun NavController() {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") { LoginScreen(navController) }
            composable("principal") { PrincipalScreen(navController) }
            composable("programar") { ProgramarScreen(navController) }
            composable("sonidos") { SonidosScreen(navController) }
            composable("Notificaciones") { NotificacionesScreen(navController) }
        }

        NotificacionEnPantalla()
    }
}
