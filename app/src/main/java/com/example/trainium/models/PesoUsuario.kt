package com.example.trainium.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PesoUsuario(
    val id: Int? = null,
    @SerialName("id_usuario")
    val idUsuario: Int,
    val peso: Double,
    val fecha: String
)
