package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.firebaseapp.FirebaseLogin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(navController: NavHostController) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            // TÍTULO NUEVO
            Text(
                text = "Iniciar Sesión",
                fontSize = 28.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(26.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation() // <-- Aquí se oculta la contraseña
            )

            Spacer(modifier = Modifier.height(18.dp))

            // BOTÓN REGISTRAR (NEGRO)
            Button(
                onClick = {
                    loading = true
                    FirebaseLogin.registrar(email, password) { success, msg ->
                        loading = false
                        if (success) {
                            navController.navigate("principal") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else errorMsg = msg
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Registrar", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN INICIAR SESIÓN (NEGRO)
            Button(
                onClick = {
                    loading = true
                    FirebaseLogin.iniciarSesion(email, password) { success, msg ->
                        loading = false
                        if (success) {
                            navController.navigate("principal") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else errorMsg = msg
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Iniciar sesión", color = Color.White)
            }

            if (loading) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cargando...")
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Error: $errorMsg",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
