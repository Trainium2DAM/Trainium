package com.example.trainium.data.repository

import com.example.trainium.DbColumns
import com.example.trainium.DbTables
import com.example.trainium.SupabaseClient
import com.example.trainium.models.Maquina
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
        val body = buildJsonObject {
            put("nombre", maquina.nombre)
            put("estado", maquina.estado)
            put("operativa", maquina.operativa)
            maquina.tipo?.let { put("tipo", it) }
            maquina.descripcion?.let { put("descripcion", it) }
            maquina.foto?.let { put("foto", it) }
            maquina.mantenimiento_desde?.let { put("mantenimiento_desde", it) }
            maquina.mantenimiento_hasta?.let { put("mantenimiento_hasta", it) }
        }
        SupabaseClient.client.from(DbTables.MAQUINAS).insert(body)
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
