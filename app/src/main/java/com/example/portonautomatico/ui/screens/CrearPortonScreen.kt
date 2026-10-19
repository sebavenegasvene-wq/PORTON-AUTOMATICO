package com.example.portonautomatico.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPortonScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var idPorton by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "CREAR PORTÓN",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Registra un nuevo portón",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del portón") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = idPorton,
                        onValueChange = { idPorton = it },
                        label = { Text("ID del portón (ej: 1234)") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Clave del portón (mín 6)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (idPorton.isBlank() || clave.isBlank() || nombre.isBlank()) {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (idPorton.length < 4) {
                                Toast.makeText(context, "El ID debe tener al menos 4 dígitos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (clave.length < 6) {
                                Toast.makeText(context, "La clave debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            loading = true
                            val ref = db.collection("portones").document(idPorton.trim())

                            // Verificar si ya existe
                            ref.get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (task.result?.exists() == true) {
                                        loading = false
                                        Toast.makeText(context, "Ya existe un portón con ese ID", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Crear el portón
                                        val datos = mapOf(
                                            "id" to idPorton.trim(),
                                            "clave" to clave.trim(),
                                            "nombre" to nombre.trim(),
                                            "creadoPor" to (auth.currentUser?.email ?: ""),
                                            "estado" to false,
                                            "ultimaAccionPor" to "",
                                            "ultimaAccionEn" to 0L
                                        )
                                        ref.set(datos).addOnCompleteListener { t2 ->
                                            loading = false
                                            if (t2.isSuccessful) {
                                                Toast.makeText(context, "¡Portón creado!", Toast.LENGTH_SHORT).show()
                                                navController.navigate("access") {
                                                    popUpTo("crearPorton") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(context, "Error: ${t2.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                } else {
                                    loading = false
                                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2027))
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("CREAR PORTÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = {
                        navController.navigate("access") {
                            popUpTo("crearPorton") { inclusive = true }
                        }
                    }) {
                        Text(
                            "Volver",
                            color = Color(0xFF2C5364),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}