package com.example.trainium2.data.repository

import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.SupabaseClient
import com.example.trainium2.models.Pago
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PagoRepository {

    suspend fun getByUser(userId: Int): List<Pago> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PAGOS)
            .select {
                filter { eq(DbColumns.ID_USUARIO, userId) }
            }.decodeList<Pago>()
    }

    suspend fun insert(pago: Pago) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from(DbTables.PAGOS).insert(pago)
    }
}
