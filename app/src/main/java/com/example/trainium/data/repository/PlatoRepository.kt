package com.example.trainium.data.repository

import com.example.trainium.DbColumns
import com.example.trainium.DbTables
import com.example.trainium.SupabaseClient
import com.example.trainium.models.Plato
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlatoRepository {

    suspend fun getApproved(): List<Plato> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PLATOS)
            .select {
                filter {
                    eq(DbColumns.VISIBILIDAD, true)
                    eq(DbColumns.ACEPTADO, true)
                }
            }.decodeList<Plato>()
    }

    suspend fun getPending(): List<Plato> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PLATOS)
            .select { filter { eq(DbColumns.ACEPTADO, false) } }
            .decodeList<Plato>()
    }

    suspend fun insert(plato: Plato) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PLATOS).insert(plato)
    }

    suspend fun approve(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PLATOS).update({
            set(DbColumns.ACEPTADO, true)
        }) { filter { eq(DbColumns.ID, id) } }
    }

    suspend fun reject(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PLATOS).delete {
            filter { eq(DbColumns.ID, id) }
        }
    }
}
