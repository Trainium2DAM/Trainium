package com.example.trainium2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reserva(
    val id: Int? = null,
    @SerialName("id_usuario")
    val idUsuario: Int,
    @SerialName("id_maquina")
    val idMaquina: Int,
    val fecha: String,
    @SerialName("hora_inicio")
    val horaInicio: String,
    @SerialName("hora_fin")
    val horaFin: String,
    val estado: Boolean = true
)

@Serializable
data class ReservaConDetalles(
    val id: Int,
    val fecha: String,
    @SerialName("hora_inicio")
    val horaInicio: String,
    @SerialName("hora_fin")
    val horaFin: String,
    val estado: Boolean,
    @SerialName("usuarios")
    val usuario: Usuario? = null,
    @SerialName("maquinas")
    val maquina: Maquina? = null
)
