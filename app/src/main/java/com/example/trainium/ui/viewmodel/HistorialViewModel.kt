package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.di.ServiceLocator
import com.example.trainium.models.Pago
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialViewModel : ViewModel() {
    var pagos by mutableStateOf<List<Pago>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun loadPagos(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                pagos = withContext(Dispatchers.IO) {
                    ServiceLocator.pagoRepository.getByUser(userId)
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
