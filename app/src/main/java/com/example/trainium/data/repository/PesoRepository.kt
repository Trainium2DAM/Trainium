package com.example.trainium.data.repository

import com.example.trainium.DbColumns
import com.example.trainium.DbTables
import com.example.trainium.SupabaseClient
import com.example.trainium.models.PesoUsuario
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PesoRepository {

    suspend fun getByUser(userId: Int): List<PesoUsuario> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PESO_USUARIO)
            .select { filter { eq(DbColumns.ID_USUARIO, userId) } }
            .decodeList<PesoUsuario>()
    }

    suspend fun insert(idUsuario: Int, peso: Double, fecha: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PESO_USUARIO).insert(
            PesoUsuario(idUsuario = idUsuario, peso = peso, fecha = fecha)
        )
    }

    suspend fun update(id: Int, peso: Double) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PESO_USUARIO).update({
            set("peso", peso)
        }) { filter { eq(DbColumns.ID, id) } }
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PESO_USUARIO).delete {
            filter { eq(DbColumns.ID, id) }
        }
    }
}
