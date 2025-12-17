package com.example.myapplication.Screens

import androidx.compose.foundation.clickable
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
import java.util.Date
import java.util.Locale

@Composable
fun PrincipalScreen(navController: NavHostController) {

    var alarmaActiva by remember { mutableStateOf(false) }
    var puertaPrincipalAbierta by remember { mutableStateOf(false) }
    var puertaTraseraAbierta by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseManager.listenFirebase("alarma") { value ->
            alarmaActiva = value == "true"
        }
        FirebaseManager.listenFirebase("puertaPrincipal") { value ->
            puertaPrincipalAbierta = value == "true"
        }
        FirebaseManager.listenFirebase("puertaTrasera") { value ->
            puertaTraseraAbierta = value == "true"
        }
    }

    fun guardarEstado() {
        FirebaseManager.escribirFirebase("alarma", alarmaActiva)
        FirebaseManager.escribirFirebase("puertaPrincipal", puertaPrincipalAbierta)
        FirebaseManager.escribirFirebase("puertaTrasera", puertaTraseraAbierta)
    }

    fun notificar(mensaje: String, abierta: Boolean) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date())
        val id = System.currentTimeMillis().toString()
        FirebaseManager.escribirFirebase("notificaciones/$id", "$mensaje|$hora|$abierta")
    }

    val todoCerrado = !puertaPrincipalAbierta && !puertaTraseraAbierta

    Scaffold(bottomBar = { BottomBar(navController) }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Alarma de Casa", fontSize = 30.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Spacer(modifier = Modifier.height(30.dp))
            Text("Estado general del hogar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

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
                Text(if (alarmaActiva) "Desactivar alarma" else "Activar alarma", fontSize = 18.sp)
            }

            Spacer(Modifier.height(20.dp))

            EstadoElemento(
                nombre = "Puerta Principal",
                abierto = puertaPrincipalAbierta
            ) {
                puertaPrincipalAbierta = !puertaPrincipalAbierta
                guardarEstado()
                notificar(
                    "Puerta Principal ${if (puertaPrincipalAbierta) "abierta" else "cerrada"}",
                    puertaPrincipalAbierta
                )
            }

            EstadoElemento(
                nombre = "Puerta Trasera",
                abierto = puertaTraseraAbierta
            ) {
                puertaTraseraAbierta = !puertaTraseraAbierta
                guardarEstado()
                notificar(
                    "Puerta Trasera ${if (puertaTraseraAbierta) "abierta" else "cerrada"}",
                    puertaTraseraAbierta
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                fontSize = 16.sp
            )
        }
    }
}
