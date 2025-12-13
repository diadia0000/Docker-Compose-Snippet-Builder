package com.example.docker.network

// 檔案: network/SupabaseClient.kt
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    // 建議將這些放在 local.properties 或 BuildConfig 以確保安全，測試時可先直接填
    private const val SUPABASE_URL = "https://你的專案ID.supabase.co"
    private const val SUPABASE_KEY = "你的_ANON_KEY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // 設定 JSON 序列化規則 (忽略未知的欄位，避免 App 崩潰)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
        })

        install(Postgrest) // 安裝資料庫模組
        install(Auth)      // 安裝驗證模組
    }
}