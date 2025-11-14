package com.example.firebaseapp

import com.google.firebase.auth.FirebaseAuth

object FirebaseLogin {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registrar(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    fun iniciarSesion(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    fun usuarioActual() = auth.currentUser
}