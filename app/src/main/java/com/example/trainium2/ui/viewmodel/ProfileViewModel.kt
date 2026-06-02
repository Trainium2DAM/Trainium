package com.example.trainium2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium2.AppConfig
import com.example.trainium2.di.ServiceLocator
import com.example.trainium2.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

private var avisoMostrado = false

class ProfileViewModel : ViewModel() {
    var usuario by mutableStateOf<Usuario?>(null)
    var avisoMantenimiento by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun loadProfile(idUsuario: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = withContext(Dispatchers.IO) {
                    ServiceLocator.usuarioRepository.getUserById(idUsuario)
                }
                usuario = user

                if (!avisoMostrado) {
                    val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())
                    val canceladas = withContext(Dispatchers.IO) {
                        ServiceLocator.reservaRepository.getCanceledToday(idUsuario, hoy)
                    }
                    val afectadas = canceladas.filter { r ->
                        r.maquina?.let { !it.operativa && it.mantenimiento_desde != null } == true
                    }
                    if (afectadas.isNotEmpty()) {
                        avisoMantenimiento = "Tienes reservas canceladas por mantenimiento en: ${
                            afectadas.mapNotNull { it.maquina?.nombre }.distinct().joinToString(", ")
                        }"
                    }
                    avisoMostrado = true
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMaintenanceWarning() { avisoMantenimiento = null }
}
