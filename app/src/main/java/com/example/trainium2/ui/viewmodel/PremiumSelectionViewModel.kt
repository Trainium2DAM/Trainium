package com.example.trainium2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium2.AppConfig
import com.example.trainium2.di.ServiceLocator
import com.example.trainium2.models.Pago
import com.example.trainium2.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class PremiumSelectionViewModel : ViewModel() {
    data class PlanInfo(val nombre: String, val precio: Double, val meses: Int)

    val planes = listOf(
        PlanInfo("Mensual", 9.99, 1),
        PlanInfo("Semestral", 49.99, 6),
        PlanInfo("Anual", 89.99, 12)
    )

    var planSeleccionado by mutableStateOf(0)
    var metodoSeleccionado by mutableStateOf("Tarjeta")
    var numeroTarjeta by mutableStateOf("")
    var fechaVencimiento by mutableStateOf("")
    var cvv by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var success by mutableStateOf(false)
    var esRenovacion by mutableStateOf(false)
    var fechaFinActual by mutableStateOf<String?>(null)
    var fechaInicioCalculada by mutableStateOf("")
    var fechaFinCalculada by mutableStateOf("")

    fun loadUser(id: Int) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                ServiceLocator.usuarioRepository.getUserById(id)
            }
            esRenovacion = user?.premium == true
            fechaFinActual = user?.fechaFin
        }
    }

    fun calculateDates() {
        val plan = planes.getOrNull(planSeleccionado) ?: return
        val cal = Calendar.getInstance()

        val finActual = fechaFinActual
        if (esRenovacion && !finActual.isNullOrBlank()) {
            val fechaActual = AppConfig.FORMAT_ISO_DATE.parse(finActual) ?: Date()
            cal.time = fechaActual
            fechaInicioCalculada = finActual
        } else {
            cal.time = Date()
            fechaInicioCalculada = AppConfig.FORMAT_ISO_DATE.format(cal.time)
        }

        cal.add(Calendar.MONTH, plan.meses)
        fechaFinCalculada = AppConfig.FORMAT_ISO_DATE.format(cal.time)
    }

    fun validateCard(): Boolean {
        val limpio = numeroTarjeta.filter { it.isDigit() }
        return limpio.length <= 16 && cvv.filter { it.isDigit() }.length <= 3
    }

    fun purchase(idUsuario: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val plan = planes.getOrNull(planSeleccionado) ?: return@launch
                val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())

                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, plan.meses)
                var fin = AppConfig.FORMAT_ISO_DATE.format(cal.time)

                val finActual = fechaFinActual
                if (esRenovacion && !finActual.isNullOrBlank()) {
                    val fechaActual = AppConfig.FORMAT_ISO_DATE.parse(finActual) ?: Date()
                    cal.time = fechaActual
                    cal.add(Calendar.MONTH, plan.meses)
                    fin = AppConfig.FORMAT_ISO_DATE.format(cal.time)
                }

                val pago = Pago(
                    idUsuario = idUsuario,
                    monto = plan.precio,
                    fechaPago = hoy,
                    tipo = plan.nombre,
                    metodoPago = metodoSeleccionado
                )
                withContext(Dispatchers.IO) {
                    ServiceLocator.pagoRepository.insert(pago)
                    ServiceLocator.usuarioRepository.setPremium(idUsuario, hoy, fin)
                }
                success = true
                onSuccess()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }
}
