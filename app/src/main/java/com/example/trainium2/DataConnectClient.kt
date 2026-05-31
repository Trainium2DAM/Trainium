package com.example.trainium2

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataConnectClient {
    private const val API_KEY = "AIzaSyCLXU9kHPUTRCaBKAlzYXKzKkC37-5Ikbs" 
    private const val APP_ID = "1:689673187580:android:de07c7521cac49a2bd6779"
    
    // URL CORREGIDA: Se añade el subdominio regional 'europe-southwest1-' para evitar el 404
    private const val ENDPOINT = "https://europe-southwest1-dataconnect.googleapis.com/v1beta/projects/bbdd-practicas/locations/europe-southwest1/services/bbdd-practicas-service/connectors/default:executeGraphql"

    suspend fun execute(
        query: String,
        variables: Map<String, Any?> = emptyMap(),
        operationName: String? = null
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            Log.d("DataConnect", ">>>> LLAMANDO A: $ENDPOINT")
            
            val url = URL(ENDPOINT)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", API_KEY)
            conn.setRequestProperty("x-firebase-gmpid", APP_ID)
            conn.doOutput = true

            val body = JSONObject().apply {
                put("query", query)
                if (variables.isNotEmpty()) {
                    val varsJson = JSONObject()
                    variables.forEach { (k, v) -> varsJson.put(k, v ?: JSONObject.NULL) }
                    put("variables", varsJson)
                }
                if (operationName != null) put("operationName", operationName)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalle"
                Log.e("DataConnect", "ERROR $responseCode: $errorMsg")
                return@withContext null
            }

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            Log.d("DataConnect", "EXITO: $responseText")
            return@withContext JSONObject(responseText)
        } catch (e: Exception) {
            Log.e("DataConnect", "FALLO DE RED: ${e.message}", e)
            null
        }
    }
}
