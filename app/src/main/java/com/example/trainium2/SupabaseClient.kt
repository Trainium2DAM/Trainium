package com.example.trainium2

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    private const val SUPABASE_URL = "https://zuvattchpylwyclbwahe.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_JjSO4v8MjwWfTxKNuRd7fw_uXxeK7Q2"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
