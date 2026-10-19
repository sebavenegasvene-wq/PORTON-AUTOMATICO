package com.example.portonautomatico.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.portonautomatico.ui.PortonViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavHostController,
    portonId: String,
    viewModel: PortonViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val mensajeRemoto by viewModel.mensajeRemoto.collectAsStateWithLifecycle()

    // 👇 Inicializar el ViewModel con el ID del portón
    LaunchedEffect(portonId) {
        viewModel.inicializar(portonId)
    }

    val abierto = estado.estado
    var cargando by remember { mutableStateOf(false) }

    val colorEstado by animateColorAsState(
        if (abierto) Color(0xFF2E7D32) else Color(0xFFC62828),
        animationSpec = tween(600), label = "color"
    )

    LaunchedEffect(mensajeRemoto) {
        mensajeRemoto?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    fun autenticar(callback: (Boolean) -> Unit) {
        val activity = context as? FragmentActivity
        if (activity == null) {
            Toast.makeText(context, "Error interno: contexto inválido", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        val manager = BiometricManager.from(context)
        val autenticadores = BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val estadoBio = manager.canAuthenticate(autenticadores)
        if (estadoBio != BiometricManager.BIOMETRIC_SUCCESS) {
            val mensaje = when (estadoBio) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "Este dispositivo no soporta bloqueo de pantalla"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    "El bloqueo de pantalla no está disponible"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "Configura un PIN, patrón o contraseña en tu teléfono primero"
                else -> "No se puede usar el bloqueo del teléfono"
            }
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            callback(false)
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    callback(true)
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
                override fun onAuthenticationFailed() {
                    Toast.makeText(context, "No reconocido, intenta de nuevo", Toast.LENGTH_SHORT).show()
                }
            })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar para abrir")
            .setSubtitle("Ingresa la clave de tu teléfono")
            .setAllowedAuthenticators(autenticadores)
            .build()

        prompt.authenticate(info)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF5F7FA), Color(0xFFE8EEF5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Portón $portonId",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2027)
                    )
                    Text(
                        auth.currentUser?.email ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                Row {
                    TextButton(onClick = {
                        navController.navigate("access") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Text(
                            "Cambiar",
                            color = Color(0xFF0F2027),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Salir", tint = Color(0xFFC62828))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(colorEstado.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (abierto) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (abierto) "PORTÓN ABIERTO" else "PORTÓN CERRADO",
                        color = colorEstado,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (abierto) "Toca el botón para cerrarlo" else "Necesitas la clave para abrirlo",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    PortonDibujo(abierto = abierto)

                    Spacer(Modifier.height(16.dp))

                    if (estado.ultimaAccionEn > 0) {
                        val fecha = remember(estado.ultimaAccionEn) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(estado.ultimaAccionEn))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0F4F8),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Última acción",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                                Text(
                                    estado.ultimaAccionPor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F2027)
                                )
                                Text(
                                    fecha,
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (abierto) {
                        viewModel.cambiarEstado(false)
                        Toast.makeText(context, "Portón cerrado", Toast.LENGTH_SHORT).show()
                    } else {
                        cargando = true
                        autenticar { ok ->
                            cargando = false
                            if (ok) {
                                viewModel.cambiarEstado(true)
                                Toast.makeText(context, "Portón abierto", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        if (cargando) 0.dp else 8.dp,
                        RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (abierto) Color(0xFFC62828) else Color(0xFF2E7D32)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        Modifier.size(26.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (abierto) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (abierto) "CERRAR PORTÓN" else "ABRIR PORTÓN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                if (abierto) "Toca para cerrar el portón" else "Toca para verificar con la clave del teléfono",
                fontSize = 11.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun PortonDibujo(abierto: Boolean) {
    val progreso by animateFloatAsState(
        targetValue = if (abierto) 1f else 0f,
        animationSpec = tween(900), label = "porton"
    )
    val alturaBarrotes = 170f

    Canvas(modifier = Modifier.size(280.dp, 220.dp)) {
        val ancho = size.width
        val alto = size.height
        val baseY = alto - 20f
        val topY = baseY - alturaBarrotes

        drawRect(
            color = Color(0x22000000),
            topLeft = Offset(ancho * 0.05f, baseY),
            size = Size(ancho * 0.9f, 6f)
        )

        drawLine(
            color = Color(0xFF455A64),
            start = Offset(0f, baseY),
            end = Offset(ancho, baseY),
            strokeWidth = 5f
        )

        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(ancho * 0.05f, topY - 20f),
            size = Size(20f, alturaBarrotes + 20f),
            cornerRadius = CornerRadius(4f)
        )
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(ancho * 0.87f, topY - 20f),
            size = Size(20f, alturaBarrotes + 20f),
            cornerRadius = CornerRadius(4f)
        )

        drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(ancho * 0.05f, topY - 18f),
            size = Size(ancho * 0.83f, 12f),
            cornerRadius = CornerRadius(6f)
        )

        val numBarrotes = 7
        val inicioX = ancho * 0.13f
        val finX = ancho * 0.84f
        val espacio = (finX - inicioX) / (numBarrotes - 1)

        for (i in 0 until numBarrotes) {
            val x = inicioX + i * espacio
            val desplazamiento = -progreso * (alturaBarrotes - 30f)

            val colorBarrote = if (progreso > 0.5f) Color(0xFF2E7D32) else Color(0xFF546E7A)

            drawLine(
                color = colorBarrote,
                start = Offset(x, topY - desplazamiento),
                end = Offset(x, baseY - desplazamiento),
                strokeWidth = 9f
            )
            drawCircle(
                color = if (progreso > 0.5f) Color(0xFF1B5E20) else Color(0xFF37474F),
                radius = 6.5f,
                center = Offset(x, topY - desplazamiento)
            )
        }

        drawRoundRect(
            color = if (abierto) Color(0x332E7D32) else Color(0x33C62828),
            topLeft = Offset(ancho * 0.05f, topY - 20f),
            size = Size(ancho * 0.83f, alturaBarrotes + 25f),
            cornerRadius = CornerRadius(12f),
            style = Stroke(width = 4f)
        )
    }
}