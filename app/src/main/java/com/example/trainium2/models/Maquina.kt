package com.example.trainium2.models

import kotlinx.serialization.Serializable

@Serializable
data class Maquina(
    val id: Int,
    val nombre: String,
    val tipo: String? = null,
    val estado: Int = 0,
    val descripcion: String? = null,
    val operativa: Boolean = true,
    val mantenimiento_desde: String? = null,
    val mantenimiento_hasta: String? = null
)
