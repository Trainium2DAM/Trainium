package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.AppConfig
import com.example.trainium.di.ServiceLocator
import com.example.trainium.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

private var avisoMostrado = false

class ProfileViewModel : ViewModel() {
    var usuario by mutableStateOf<Usuario?>(null)
    var avisoMantenimiento by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var diasRestantes by mutableStateOf(-1)

    fun loadProfile(idUsuario: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = withContext(Dispatchers.IO) {
                    ServiceLocator.usuarioRepository.getUserById(idUsuario)
                }
                usuario = user

                diasRestantes = user?.fechaFin?.let { fin ->
                    val hoy = AppConfig.FORMAT_ISO_DATE.parse(AppConfig.FORMAT_ISO_DATE.format(Date()))!!
                    val finDate = AppConfig.FORMAT_ISO_DATE.parse(fin) ?: return@let -1
                    TimeUnit.MILLISECONDS.toDays(finDate.time - hoy.time).toInt()
                } ?: -1

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
