package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.di.ServiceLocator
import com.example.trainium.models.Maquina
import com.example.trainium.models.Reserva
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MaquinasViewModel : ViewModel() {
    var listaMaquinas by mutableStateOf<List<Maquina>>(emptyList())
    var isLoading by mutableStateOf(false)
    var showingDialogAdd by mutableStateOf(false)
    var nuevoNombre by mutableStateOf("")
    var nuevoTipo by mutableStateOf("")
    var nuevaDesc by mutableStateOf("")
    var nuevaFoto by mutableStateOf<String?>(null)
    var maquinaParaMantenimiento by mutableStateOf<Int?>(null)
    var fechaDesdeM by mutableStateOf("")
    var horaDesdeM by mutableStateOf("")
    var fechaHastaM by mutableStateOf("")
    var horaHastaM by mutableStateOf("")
    var maquinaParaReservar by mutableStateOf<Int?>(null)
    var fechaSeleccionada by mutableStateOf("")
    var horaSeleccionada by mutableStateOf("")
    var reservasDeLaMaquina by mutableStateOf<List<Reserva>>(emptyList())
    var reservasDelUsuario by mutableStateOf<List<Reserva>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)

    // Snackbar state: set to the created Reserva when booking succeeds
    var reservaExitosa by mutableStateOf<Reserva?>(null)

    fun loadMachines() {
        viewModelScope.launch {
            isLoading = true
            try {
                listaMaquinas = withContext(Dispatchers.IO) {
                    ServiceLocator.maquinaRepository.getAll()
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun addMachine(nombre: String, tipo: String, desc: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val maquina = Maquina(id = 0, nombre = nombre, tipo = tipo, descripcion = desc, foto = nuevaFoto)
                withContext(Dispatchers.IO) {
                    ServiceLocator.maquinaRepository.insert(maquina)
                }
                loadMachines()
                showingDialogAdd = false
                nuevoNombre = ""; nuevoTipo = ""; nuevaDesc = ""; nuevaFoto = null
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteMachine(id: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.maquinaRepository.delete(id)
                }
                loadMachines()
            } catch (_: Exception) {
            }
        }
    }

    fun startMaintenance(machineId: Int, desde: String, hasta: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.maquinaRepository.activateMaintenance(machineId, desde, hasta)
                    ServiceLocator.reservaRepository.cancelByMachine(machineId)
                }
                loadMachines()
                maquinaParaMantenimiento = null
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun endMaintenance(machineId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.maquinaRepository.deactivateMaintenance(machineId)
                }
                loadMachines()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun loadReservationsForSlot(machineId: Int, date: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val (deMaquina, deUsuario) = withContext(Dispatchers.IO) {
                    val m = ServiceLocator.reservaRepository.getByMachineAndDate(machineId, date)
                    val u = ServiceLocator.reservaRepository.getByUserAndDate(machineId, date)
                    Pair(m, u)
                }
                reservasDeLaMaquina = deMaquina
                reservasDelUsuario = deUsuario
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun reserveTimeSlot(machineId: Int, userId: Int, date: String, horaInicio: String, horaFin: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val reserva = Reserva(idUsuario = userId, idMaquina = machineId, fecha = date, horaInicio = horaInicio, horaFin = horaFin)
                val created = withContext(Dispatchers.IO) {
                    ServiceLocator.reservaRepository.insert(reserva)
                }
                reservaExitosa = created ?: reserva
                maquinaParaReservar = null
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelUltimaReserva() {
        val reservaId = reservaExitosa?.id ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.reservaRepository.delete(reservaId)
                }
            } catch (_: Exception) {}
        }
        reservaExitosa = null
    }

    fun clearReservaExitosa() { reservaExitosa = null }
    fun clearError() { errorMessage = null }
}
