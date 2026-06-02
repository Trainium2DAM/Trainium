package com.example.trainium2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium2.AppConfig
import com.example.trainium2.di.ServiceLocator
import com.example.trainium2.models.ReservaConDetalles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservasViewModel : ViewModel() {
    var todasLasReservas by mutableStateOf<List<ReservaConDetalles>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var filtroSeleccionado by mutableStateOf("upcoming")
    var fechaFiltroManual by mutableStateOf("")

    fun loadReservations(isAdmin: Boolean, userId: Int) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                todasLasReservas = withContext(Dispatchers.IO) {
                    ServiceLocator.reservaRepository.getAllWithDetails(isAdmin, userId)
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteReservation(id: Int, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.reservaRepository.delete(id)
                }
                onDeleted()
            } catch (_: Exception) {
            }
        }
    }

    fun setFilter(filter: String) {
        filtroSeleccionado = filter
    }

    fun setDateFilter(date: String) {
        fechaFiltroManual = date
    }

    private fun isPast(r: ReservaConDetalles): Boolean {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val horaFin5 = r.horaFin.take(5)
            val reservaEnd = fmt.parse("${r.fecha} $horaFin5") ?: return false
            reservaEnd.before(Date())
        } catch (_: Exception) {
            false
        }
    }

    fun getFilteredReservations(): List<ReservaConDetalles> {
        val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())
        return when (filtroSeleccionado) {
            "today"    -> todasLasReservas.filter { it.fecha == hoy }
            "upcoming" -> todasLasReservas.filter { !isPast(it) }
            "past"     -> todasLasReservas.filter { isPast(it) }
            "date"     -> todasLasReservas.filter { it.fecha == fechaFiltroManual }
            else       -> todasLasReservas
        }.sortedWith(compareBy({ it.fecha }, { it.horaInicio }))
    }

    fun isPastReservation(r: ReservaConDetalles): Boolean = isPast(r)
}
