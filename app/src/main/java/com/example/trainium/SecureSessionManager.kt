package com.example.trainium

import android.content.Context
import android.content.SharedPreferences
import com.example.trainium.models.SesionData

object SecureSessionManager {
    private var prefs: SharedPreferences? = null

    fun setContext(context: Context) {
        prefs = context.getSharedPreferences(AppConfig.SESSION_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun iniciarSesion(id: Int, nombre: String, isAdmin: Boolean, isPremium: Boolean): Boolean {
        prefs?.edit()?.apply {
            putInt("userId", id)
            putString("userName", nombre)
            putBoolean("isAdmin", isAdmin)
            putBoolean("isPremium", isPremium)
            apply()
        }
        val savedId = prefs?.getInt("userId", -1) ?: -1
        return savedId == id
    }

    fun obtenerSesion(): SesionData? {
        val id = prefs?.getInt("userId", -1) ?: -1
        val nombre = prefs?.getString("userName", null) ?: return null
        val admin = prefs?.getBoolean("isAdmin", false) ?: false
        val premium = prefs?.getBoolean("isPremium", false) ?: false
        return if (id != -1) SesionData(id, nombre, admin, premium, "") else null
    }

    fun cerrarSesion() {
        prefs?.edit()?.clear()?.apply()
    }
}
