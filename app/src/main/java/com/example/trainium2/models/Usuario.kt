package com.example.trainium2.models

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Int,
    val nombre: String,
    val dni: String,
    val contraseniaHash: String,
    val admin: Int,
    val premium: Boolean,
    val email: String? = null,
    val telefono: String? = null,
    val foto: String? = null // Nuevo campo para la imagen en Base64
)
