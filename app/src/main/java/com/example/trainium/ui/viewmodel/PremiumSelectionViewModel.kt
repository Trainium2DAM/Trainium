package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.AppConfig
import com.example.trainium.di.ServiceLocator
import com.example.trainium.models.Pago
import com.example.trainium.models.Usuario
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

    fun clearError() { errorMessage = null }

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

    var errorMessage by mutableStateOf<String?>(null)

    fun validateCard(): Boolean {
        val cardClean = numeroTarjeta.filter { it.isDigit() }
        if (cardClean.length < 13 || cardClean.length > 16) {
            errorMessage = "invalid_card_number"
            return false
        }
        val cvvClean = cvv.filter { it.isDigit() }
        if (cvvClean.length != 3) {
            errorMessage = "invalid_cvv"
            return false
        }
        val parts = fechaVencimiento.split("/")
        if (parts.size != 2) {
            errorMessage = "invalid_expiry_date"
            return false
        }
        val mes = parts[0].toIntOrNull()
        val anio = parts[1].toIntOrNull()
        if (mes == null || anio == null || mes < 1 || mes > 12) {
            errorMessage = "invalid_expiry_date"
            return false
        }
        val cal = Calendar.getInstance()
        val anioActual = cal.get(Calendar.YEAR) % 100
        val mesActual = cal.get(Calendar.MONTH) + 1
        if (anio < anioActual || (anio == anioActual && mes < mesActual)) {
            errorMessage = "card_expired"
            return false
        }
        return true
    }

    fun purchase(idUsuario: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                if (!validateCard()) return@launch
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
