package com.example.portonautomatico.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RegistroPorton(
    val estado: Boolean = false,
    val ultimaAccionPor: String = "",
    val ultimaAccionEn: Long = 0L
)

class PortonViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _estado = MutableStateFlow(RegistroPorton())
    val estado: StateFlow<RegistroPorton> = _estado.asStateFlow()

    private val _mensajeRemoto = MutableStateFlow<String?>(null)
    val mensajeRemoto: StateFlow<String?> = _mensajeRemoto.asStateFlow()

    private var primerSnapshot = true
    private var portonId: String? = null

    // 👇 NUEVO: Inicializar con un ID de portón específico
    fun inicializar(id: String) {
        if (portonId == id) return  // ya está escuchando ese portón
        portonId = id
        primerSnapshot = true
        observarPorton(id)
    }

    private fun observarPorton(id: String) {
        val ref = db.collection("portones").document(id)

        viewModelScope.launch {
            callbackFlow {
                val listener = ref.addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val data = snapshot?.data
                    val nuevo = RegistroPorton(
                        estado = data?.get("estado") as? Boolean ?: false,
                        ultimaAccionPor = data?.get("ultimaAccionPor") as? String ?: "",
                        ultimaAccionEn = data?.get("ultimaAccionEn") as? Long ?: 0L
                    )

                    if (!primerSnapshot && nuevo.ultimaAccionPor != auth.currentUser?.email) {
                        val accion = if (nuevo.estado) "ABRIÓ" else "CERRÓ"
                        _mensajeRemoto.value = "🔔 ${nuevo.ultimaAccionPor} $accion el portón"
                    }
                    primerSnapshot = false
                    _estado.value = nuevo
                    trySend(nuevo)
                }
                awaitClose { listener.remove() }
            }.collectLatest { }
        }
    }

    fun cambiarEstado(nuevoEstado: Boolean) {
        val id = portonId ?: return
        val ref = db.collection("portones").document(id)
        val email = auth.currentUser?.email ?: "Desconocido"
        ref.update(
            mapOf(
                "estado" to nuevoEstado,
                "ultimaAccionPor" to email,
                "ultimaAccionEn" to System.currentTimeMillis()
            )
        )
    }

    fun limpiarMensaje() { _mensajeRemoto.value = null }
}