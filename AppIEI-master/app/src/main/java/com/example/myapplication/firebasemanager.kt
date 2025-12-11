package com.example.firebaseapp

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {

    private val database: DatabaseReference = Firebase.database.reference

    fun escribirFirebase(nombreCampo: String, valor: Any) {
        database.child("configuracion").child(nombreCampo)
            .setValue(valor)
            .addOnSuccessListener {
                Log.d("Firebase", "Dato guardado correctamente en $nombreCampo")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar en $nombreCampo", e)
            }
    }

    fun leerFirebase(nombreCampo: String, onData: (String) -> Unit) {
        database.child("configuracion").child(nombreCampo)
            .get()
            .addOnSuccessListener { snapshot ->
                onData(snapshot.value?.toString() ?: "")
            }
            .addOnFailureListener {
                onData("")
            }
    }

    fun listenFirebase(nombreCampo: String, onData: (String) -> Unit) {
        val ref = database.child("configuracion").child(nombreCampo)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value?.toString() ?: ""
                onData(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error escuchando $nombreCampo", error.toException())
            }
        })
    }
}
