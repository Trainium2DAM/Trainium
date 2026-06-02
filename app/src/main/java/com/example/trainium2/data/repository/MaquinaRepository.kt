package com.example.trainium2.data.repository

import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.SupabaseClient
import com.example.trainium2.models.Maquina
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MaquinaRepository {

    suspend fun getAll(): List<Maquina> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.MAQUINAS)
            .select()
            .decodeList<Maquina>()
    }

    suspend fun insert(maquina: Maquina) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.MAQUINAS).insert(maquina)
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.MAQUINAS).delete {
            filter { eq(DbColumns.ID, id) }
        }
    }

    suspend fun activateMaintenance(id: Int, desde: String, hasta: String) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.MAQUINAS).update({
            set("operativa", false)
            set("mantenimiento_desde", desde)
            set("mantenimiento_hasta", hasta)
        }) { filter { eq(DbColumns.ID, id) } }
    }

    suspend fun deactivateMaintenance(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.MAQUINAS).update({
            set("operativa", true)
            set("mantenimiento_desde", null as String?)
            set("mantenimiento_hasta", null as String?)
        }) { filter { eq(DbColumns.ID, id) } }
    }
}
