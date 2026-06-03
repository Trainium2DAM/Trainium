package com.example.trainium.di

import com.example.trainium.data.repository.AuthRepository
import com.example.trainium.data.repository.MaquinaRepository
import com.example.trainium.data.repository.PagoRepository
import com.example.trainium.data.repository.PesoRepository
import com.example.trainium.data.repository.PlatoRepository
import com.example.trainium.data.repository.ReservaRepository
import com.example.trainium.data.repository.UsuarioRepository

object ServiceLocator {
    val authRepository by lazy { AuthRepository() }
    val usuarioRepository by lazy { UsuarioRepository() }
    val maquinaRepository by lazy { MaquinaRepository() }
    val reservaRepository by lazy { ReservaRepository() }
    val platoRepository by lazy { PlatoRepository() }
    val pesoRepository by lazy { PesoRepository() }
    val pagoRepository by lazy { PagoRepository() }
}
