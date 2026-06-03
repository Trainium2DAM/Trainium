package com.example.trainium.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plato(
    val id: Int? = null,
    @SerialName("id_usuario")
    val idUsuario: Int? = null,
    val nombre: String,
    val descripcion: String? = null,
    val calorias: Double? = null,
    @SerialName("imagen_url")
    val imagenUrl: String? = null,
    @SerialName("fecha_subida")
    val fechaSubida: String? = null,
    val visibilidad: Boolean = true,
    val tiempo: String? = null,      // Tiempo de preparación (ej: "30 minutos")
    val aceptado: Boolean = false    // Estado de aprobación por el Administrador
)
