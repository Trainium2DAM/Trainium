package com.example.trainium2.di

import com.example.trainium2.data.repository.AuthRepository
import com.example.trainium2.data.repository.MaquinaRepository
import com.example.trainium2.data.repository.PagoRepository
import com.example.trainium2.data.repository.PesoRepository
import com.example.trainium2.data.repository.PlatoRepository
import com.example.trainium2.data.repository.ReservaRepository
import com.example.trainium2.data.repository.UsuarioRepository

object ServiceLocator {
    val authRepository by lazy { AuthRepository() }
    val usuarioRepository by lazy { UsuarioRepository() }
    val maquinaRepository by lazy { MaquinaRepository() }
    val reservaRepository by lazy { ReservaRepository() }
    val platoRepository by lazy { PlatoRepository() }
    val pesoRepository by lazy { PesoRepository() }
    val pagoRepository by lazy { PagoRepository() }
}
