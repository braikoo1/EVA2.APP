package com.example.myapplication.Composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BottomBar(navController: NavHostController) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Home", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("principal") })
            Text("Programar Alarmas", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("programar") })
            Text("Sonidos", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("sonidos") })
        }
    }
}
