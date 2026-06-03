package com.example.trainium.models

import kotlinx.serialization.Serializable

@Serializable
data class Maquina(
    val id: Int,
    val nombre: String,
    val tipo: String? = null,
    val estado: Int = 0,
    val descripcion: String? = null,
    val operativa: Boolean = true,
    val foto: String? = null,
    val mantenimiento_desde: String? = null,
    val mantenimiento_hasta: String? = null
)
