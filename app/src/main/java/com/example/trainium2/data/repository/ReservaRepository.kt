package com.example.trainium2.data.repository

import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.SupabaseClient
import com.example.trainium2.models.Reserva
import com.example.trainium2.models.ReservaConDetalles
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReservaRepository {

    suspend fun getAllWithDetails(isAdmin: Boolean, userId: Int): List<ReservaConDetalles> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("*, usuarios(*), maquinas(*)")
        SupabaseClient.client.from(DbTables.RESERVAS).select(columns) {
            if (!isAdmin) {
                filter { eq(DbColumns.ID_USUARIO, userId) }
            }
        }.decodeList<ReservaConDetalles>()
    }

    suspend fun getByMachineAndDate(machineId: Int, date: String): List<Reserva> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.RESERVAS)
            .select {
                filter {
                    eq("id_maquina", machineId)
                    eq(DbColumns.FECHA, date)
                    eq(DbColumns.ESTADO, true)
                }
            }.decodeList<Reserva>()
    }

    suspend fun getByUserAndDate(userId: Int, date: String): List<Reserva> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.RESERVAS)
            .select {
                filter {
                    eq(DbColumns.ID_USUARIO, userId)
                    eq(DbColumns.FECHA, date)
                    eq(DbColumns.ESTADO, true)
                }
            }.decodeList<Reserva>()
    }

    suspend fun getAffectedByMaintenance(machineId: Int): List<ReservaConDetalles> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("*, maquinas(*)")
        SupabaseClient.client.from(DbTables.RESERVAS)
            .select(columns) {
                filter {
                    eq("id_maquina", machineId)
                    eq(DbColumns.ESTADO, true)
                }
            }.decodeList<ReservaConDetalles>()
    }

    suspend fun insert(reserva: Reserva) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.RESERVAS).insert(reserva)
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.RESERVAS).delete {
            filter { eq(DbColumns.ID, id) }
        }
    }

    suspend fun cancelByMachine(machineId: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.RESERVAS).update({
            set(DbColumns.ESTADO, false)
        }) {
            filter {
                eq("id_maquina", machineId)
                eq(DbColumns.ESTADO, true)
            }
        }
    }

    suspend fun getCanceledToday(userId: Int, today: String): List<ReservaConDetalles> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("*, maquinas(*)")
        SupabaseClient.client.from(DbTables.RESERVAS)
            .select(columns) {
                filter {
                    eq(DbColumns.ID_USUARIO, userId)
                    eq(DbColumns.FECHA, today)
                    eq(DbColumns.ESTADO, false)
                }
            }.decodeList<ReservaConDetalles>()
    }
}
