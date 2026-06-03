package com.example.trainium.data.repository

import com.example.trainium.DbColumns
import com.example.trainium.DbTables
import com.example.trainium.SupabaseClient
import com.example.trainium.models.Usuario
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    suspend fun getUserByDni(dni: String): Usuario? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS)
            .select { filter { eq(DbColumns.DNI, dni) } }
            .decodeSingleOrNull<Usuario>()
    }

    suspend fun userExistsByDni(dni: String): Boolean = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS)
            .select { filter { eq(DbColumns.DNI, dni) } }
            .decodeList<Usuario>().isNotEmpty()
    }

    suspend fun verifyCredentials(dni: String, email: String): Usuario? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS)
            .select {
                filter {
                    eq(DbColumns.DNI, dni)
                    eq("email", email)
                }
            }.decodeSingleOrNull<Usuario>()
    }

    suspend fun updatePassword(userId: Int, newPass: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS).update({
            set("contraseniaHash", newPass)
        }) { filter { eq(DbColumns.ID, userId) } }
    }

    suspend fun insertUser(nombre: String, dni: String, email: String, pass: String, telefono: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.USUARIOS).insert(
            buildJsonObject {
                put(DbColumns.NOMBRE, nombre)
                put(DbColumns.DNI, dni)
                put("email", email)
                put("contraseniaHash", pass)
                put("telefono", telefono)
                put(DbColumns.ADMIN, 0)
                put(DbColumns.PREMIUM, false)
            }
        )
    }
}
