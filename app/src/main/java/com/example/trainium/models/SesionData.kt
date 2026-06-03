package com.example.trainium.models

data class SesionData(
    val userId: Int,
    val userName: String,
    val isAdmin: Boolean,
    val isPremium: Boolean,
    val token: String = ""
)
