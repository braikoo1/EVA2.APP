package com.example.myapplication.Composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import kotlinx.coroutines.delay

data class NotificacionBanner(val mensaje: String, val hora: String, val abierta: Boolean)

@Composable
fun NotificacionEnPantalla() {

    var ultimaNotificacion by remember { mutableStateOf<NotificacionBanner?>(null) }
    var visible by remember { mutableStateOf(false) }

    fun manejarSnapshot(snapshot: DataSnapshot) {
        val raw = snapshot.getValue(String::class.java) ?: return
        val partes = raw.split("|")

        if (partes.size == 3) {
            val mensaje = partes[0]
            val hora = partes[1]
            val abierta = partes[2] == "true"

            ultimaNotificacion = NotificacionBanner(mensaje, hora, abierta)
            visible = true
        }
    }

    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("configuracion/notificaciones")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                manejarSnapshot(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                manejarSnapshot(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LaunchedEffect(ultimaNotificacion) {
        if (ultimaNotificacion != null) {
            visible = true
            delay(3000)
            visible = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = visible && ultimaNotificacion != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val n = ultimaNotificacion!!
            val color = if (n.abierta) Color.Red else Color(0xFF4CAF50)

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 260.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(n.hora, fontSize = 12.sp, color = Color.LightGray)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        n.mensaje,
                        fontSize = 14.sp,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}