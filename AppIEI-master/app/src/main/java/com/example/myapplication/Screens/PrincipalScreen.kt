package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Composables.BottomBar
import androidx.compose.ui.unit.dp
@Composable
fun PrincipalScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Alarma de Casa", fontSize = 30.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Ultima activacion: 08:00", fontSize = 25.sp)
            Spacer(modifier = Modifier.height(5.dp))
            Text("Estado: Activada", fontSize = 25.sp)
        }
    }
}
