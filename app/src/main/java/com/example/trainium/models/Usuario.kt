package com.example.trainium.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id")
    val id: Int = 0,
    val nombre: String,
    val dni: String,
    val contraseniaHash: String,
    val admin: Int,
    val premium: Boolean,
    val email: String? = null,
    val telefono: String? = null,
    val foto: String? = null,
    @SerialName("fecha_ini_prem")
    val fechaInicio: String? = null,
    @SerialName("fecha_fin_prem")
    val fechaFin: String? = null
)
