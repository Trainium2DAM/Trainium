package com.example.trainium.data.repository

import com.example.trainium.DbColumns
import com.example.trainium.DbTables
import com.example.trainium.SupabaseClient
import com.example.trainium.models.Usuario
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UsuarioRepository {

    suspend fun getUserById(id: Int): Usuario? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS)
            .select { filter { eq(DbColumns.ID, id) } }
            .decodeSingleOrNull<Usuario>()
    }

    suspend fun updateUser(id: Int, nombre: String, email: String, telefono: String, fotoBase64: String?) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS).update({
            set(DbColumns.NOMBRE, nombre)
            set("email", email)
            set("telefono", telefono)
            set(DbColumns.FOTO, fotoBase64 ?: "")
        }) { filter { eq(DbColumns.ID, id) } }
    }

    suspend fun updatePassword(id: Int, password: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS).update({
            set("contraseniaHash", password)
        }) { filter { eq(DbColumns.ID, id) } }
    }

    suspend fun setPremium(id: Int, hoy: String, fin: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS).update({
            set(DbColumns.PREMIUM, true)
            set("fecha_ini_prem", hoy)
            set("fecha_fin_prem", fin)
        }) { filter { eq(DbColumns.ID, id) } }
    }
}
