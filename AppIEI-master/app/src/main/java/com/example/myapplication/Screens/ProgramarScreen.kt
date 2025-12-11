package com.example.myapplication.Screens

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Composables.BottomBar
import com.example.firebaseapp.FirebaseManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgramarScreen(navController: NavHostController) {
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var horaFin by rememberSaveable { mutableStateOf("") }
    var distancia by rememberSaveable { mutableStateOf(50f) }

    var horarioGuardado by rememberSaveable { mutableStateOf("") }
    var distanciaGuardada by rememberSaveable { mutableStateOf(0f) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun notificar(mensaje: String, abierta: Boolean = false) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date())
        val id = System.currentTimeMillis().toString()

        FirebaseManager.escribirFirebase(
            "notificaciones/$id",
            "$mensaje|$hora|$abierta"
        )
    }

    val pickerInicio = TimePickerDialog(
        context,
        { _: TimePicker, h, m ->
            horaInicio = String.format("%02d:%02d", h, m)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val pickerFin = TimePickerDialog(
        context,
        { _: TimePicker, h, m ->
            horaFin = String.format("%02d:%02d", h, m)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    LaunchedEffect(Unit) {
        FirebaseManager.leerFirebase("horario") { valor ->
            if (valor.isNotEmpty()) {
                horarioGuardado = valor
            }
        }
        FirebaseManager.leerFirebase("distancia") { valor ->
            if (valor.isNotEmpty()) {
                distanciaGuardada = valor.toFloatOrNull() ?: 0f
                if (distanciaGuardada > 0f) {
                    distancia = distanciaGuardada
                }
            }
        }
    }

    Scaffold(bottomBar = { BottomBar(navController) }) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Programar alarmas", fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { pickerInicio.show() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    "Seleccionar hora de activación: $horaInicio",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { pickerFin.show() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    "Seleccionar hora de desactivación: $horaFin",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    horarioGuardado = "$horaInicio - $horaFin"
                    FirebaseManager.escribirFirebase("horario", horarioGuardado)
                    notificar("Horario programado: $horarioGuardado")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Guardar horario programado", color = Color.White, fontSize = 18.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text("Distancia de detección: ${distancia.toInt()} cm", fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            Slider(
                value = distancia,
                onValueChange = { distancia = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Black,
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    distanciaGuardada = distancia
                    FirebaseManager.escribirFirebase("distancia", distanciaGuardada.toInt())
                    notificar("Distancia de detección establecida: ${distanciaGuardada.toInt()} cm")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Guardar distancia", color = Color.White, fontSize = 18.sp)
            }

            Spacer(Modifier.height(28.dp))

            if (horarioGuardado.isNotEmpty()) {
                Text("Horario Programado", fontSize = 20.sp)
                Spacer(Modifier.height(6.dp))
                Text(horarioGuardado, fontSize = 24.sp)
                Spacer(Modifier.height(14.dp))
            }

            if (distanciaGuardada > 0f) {
                Text("Distancia establecida: ${distanciaGuardada.toInt()} cm", fontSize = 24.sp)
            }
        }
    }
}
