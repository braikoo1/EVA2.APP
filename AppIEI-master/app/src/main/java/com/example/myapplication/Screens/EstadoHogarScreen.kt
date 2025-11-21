package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Composables.BottomBar
import com.example.firebaseapp.FirebaseManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EstadoHogarScreen(navController: NavHostController) {

    var alarmaActiva by remember { mutableStateOf(false) }
    var puertaPrincipalAbierta by remember { mutableStateOf(false) }
    var ventanaPrincipalAbierta by remember { mutableStateOf(false) }
    var puertaHabitacionAbierta by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    // 🟦 Cargar estado desde Firebase
    LaunchedEffect(Unit) {
        FirebaseManager.leerFirebase("alarma") { if (it.isNotEmpty()) alarmaActiva = it == "true" }
        FirebaseManager.leerFirebase("puertaPrincipal") { if (it.isNotEmpty()) puertaPrincipalAbierta = it == "true" }
        FirebaseManager.leerFirebase("ventanaPrincipal") { if (it.isNotEmpty()) ventanaPrincipalAbierta = it == "true" }
        FirebaseManager.leerFirebase("puertaHabitacion") { if (it.isNotEmpty()) puertaHabitacionAbierta = it == "true" }
    }

    fun guardarEstado() {
        FirebaseManager.escribirFirebase("alarma", alarmaActiva)
        FirebaseManager.escribirFirebase("puertaPrincipal", puertaPrincipalAbierta)
        FirebaseManager.escribirFirebase("ventanaPrincipal", ventanaPrincipalAbierta)
        FirebaseManager.escribirFirebase("puertaHabitacion", puertaHabitacionAbierta)
    }

    fun notificar(mensaje: String, abierta: Boolean) {
        val hora = sdf.format(Date())
        FirebaseManager.escribirFirebase("notificaciones/$hora",
            "$mensaje|$hora|$abierta"
        )
    }

    val todoCerrado = !puertaPrincipalAbierta && !ventanaPrincipalAbierta && !puertaHabitacionAbierta

    Scaffold(bottomBar = { BottomBar(navController) }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Estado general del hogar", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(20.dp))

            // 🟥 Tarjetas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadoCard(
                    texto = if (todoCerrado) "Todo cerrado" else "Hay aperturas",
                    color = if (todoCerrado) Color(0xFF4CAF50) else Color(0xFFF44336)
                )

                EstadoCard(
                    texto = if (alarmaActiva) "Alarma activada" else "Alarma desactivada",
                    color = if (alarmaActiva) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 🔵 Botón activar/desactivar alarma
            Button(
                onClick = {
                    alarmaActiva = !alarmaActiva
                    guardarEstado()
                    notificar("Alarma ${if (alarmaActiva) "activada" else "desactivada"}", alarmaActiva)
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (alarmaActiva) Color(0xFFF44336) else Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (alarmaActiva) "Desactivar alarma" else "Activar alarma",
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // 🔵 Estados individuales
            EstadoElemento("Puerta principal", puertaPrincipalAbierta) {
                puertaPrincipalAbierta = !puertaPrincipalAbierta
                guardarEstado()
                notificar(
                    "Puerta principal ${if (puertaPrincipalAbierta) "abierta" else "cerrada"}",
                    puertaPrincipalAbierta
                )
            }

            EstadoElemento("Ventana principal", ventanaPrincipalAbierta) {
                ventanaPrincipalAbierta = !ventanaPrincipalAbierta
                guardarEstado()
                notificar(
                    "Ventana principal ${if (ventanaPrincipalAbierta) "abierta" else "cerrada"}",
                    ventanaPrincipalAbierta
                )
            }

            EstadoElemento("Puerta habitación", puertaHabitacionAbierta) {
                puertaHabitacionAbierta = !puertaHabitacionAbierta
                guardarEstado()
                notificar(
                    "Puerta habitación ${if (puertaHabitacionAbierta) "abierta" else "cerrada"}",
                    puertaHabitacionAbierta
                )
            }
        }
    }
}

@Composable
fun EstadoCard(texto: String, color: Color) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EstadoElemento(nombre: String, abierto: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(nombre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            Text(
                if (abierto) "Abierta" else "Cerrada",
                color = if (abierto) Color.Red else Color(0xFF4CAF50),
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(if (abierto) "Cerrar" else "Abrir", color = Color.White)
            }
        }
    }
}
