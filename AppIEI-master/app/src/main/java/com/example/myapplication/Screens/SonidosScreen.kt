package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Composables.BottomBar
import com.example.firebaseapp.FirebaseManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SonidosScreen(navController: NavHostController) {
    var sonidoSeleccionado by rememberSaveable { mutableStateOf(1) }
    var modoSilencioso by rememberSaveable { mutableStateOf(false) }

    fun notificar(mensaje: String, abierta: Boolean = false) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date())
        val id = System.currentTimeMillis().toString()

        FirebaseManager.escribirFirebase(
            "notificaciones/$id",
            "$mensaje|$hora|$abierta"
        )
    }

    LaunchedEffect(Unit) {
        FirebaseManager.leerFirebase("sonido") { valor ->
            if (valor.isNotEmpty()) {
                sonidoSeleccionado = valor.toIntOrNull() ?: 1
            }
        }
        FirebaseManager.leerFirebase("silencio") { valor ->
            if (valor.isNotEmpty()) {
                modoSilencioso = valor == "true"
            }
        }
    }

    Scaffold(bottomBar = { BottomBar(navController) }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Seleccionar sonido de alarma", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..3) {
                    Button(
                        onClick = {
                            sonidoSeleccionado = i
                            FirebaseManager.escribirFirebase("sonido", i)
                            notificar("Sonido $i seleccionado")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Sonido $i", color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Sonido seleccionado: $sonidoSeleccionado", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                if (modoSilencioso) "Modo silencioso activado" else "Modo silencioso desactivado",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        modoSilencioso = true
                        FirebaseManager.escribirFirebase("silencio", true)
                        notificar("Modo silencioso activado", abierta = true)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Activar", color = Color.White, fontSize = 20.sp)
                }

                Button(
                    onClick = {
                        modoSilencioso = false
                        FirebaseManager.escribirFirebase("silencio", false)
                        notificar("Modo silencioso desactivado", abierta = false)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Desactivar", color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}
