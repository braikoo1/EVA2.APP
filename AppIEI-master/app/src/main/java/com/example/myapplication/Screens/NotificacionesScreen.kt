package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.database.*

data class Notificacion(val mensaje: String, val hora: String, val abierta: Boolean)

@Composable
fun NotificacionesScreen(navController: NavHostController) {

    var notificaciones by remember { mutableStateOf(listOf<Notificacion>()) }

    // 🔥 Escuchar cambios en tiempo real
    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("configuracion/notificaciones")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val listaTemp = mutableListOf<Notificacion>()

                for (child in snapshot.children) {
                    val raw = child.getValue(String::class.java) ?: continue

                    val partes = raw.split("|")
                    if (partes.size == 3) {
                        val mensaje = partes[0]
                        val hora = partes[1]
                        val abierta = partes[2] == "true"

                        listaTemp.add(Notificacion(mensaje, hora, abierta))
                    }
                }

                // ORDENAR por hora DESC
                notificaciones = listaTemp.sortedByDescending { it.hora }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(bottomBar = { BottomBar(navController) }) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Notificaciones recientes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(notificaciones) { n ->

                    val color = if (n.abierta) Color.Red else Color(0xFF4CAF50)

                    Text(
                        text = "-${n.hora}- ${n.mensaje}",
                        color = color,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
