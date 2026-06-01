package com.example.trainium2

import java.text.SimpleDateFormat
import java.util.Locale

object AppConfig {
    // ── Supabase ──
    const val SUPABASE_URL = "https://zuvattchpylwyclbwahe.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_JjSO4v8MjwWfTxKNuRd7fw_uXxeK7Q2"

    // ── MySQL ──
    const val MYSQL_URL = "jdbc:mysql://10.0.2.2:3306/prueba?useSSL=false&serverTimezone=UTC"
    const val MYSQL_USER = "admin_tren"
    const val MYSQL_PASS = "admin123"

    // ── Firebase / DataConnect ──
    const val FIREBASE_API_KEY = "AIzaSyCLXU9kHPUTRCaBKAlzYXKzKkC37-5Ikbs"
    const val FIREBASE_APP_ID = "1:689673187580:android:de07c7521cac49a2bd6779"
    const val DATACONNECT_URL = "https://europe-southwest1-dataconnect.googleapis.com/v1beta/projects/bbdd-practicas/locations/europe-southwest1/services/bbdd-practicas-service/connectors/default:executeGraphql"

    // ── Sesion Persistente ──
    const val SESSION_PREFS_NAME = "trainium_session"
    const val SESSION_DURATION_MS = 7L * 24L * 60L * 60L * 1000L

    // ── Imagenes ──
    const val IMAGE_QUALITY = 70

    // ── Formatos de fecha ──
    val FORMAT_ISO_DATE = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    const val FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm"
    const val FORMAT_HOUR_MINUTE = "HH:mm"
}
