package com.example.trainium.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pago(
    val id: Int? = null,
    @SerialName("id_usuario")
    val idUsuario: Int,
    val monto: Double,
    @SerialName("fecha_pago")
    val fechaPago: String,
    val tipo: String,
    @SerialName("metodo_pago")
    val metodoPago: String
)
