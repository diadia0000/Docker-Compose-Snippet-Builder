package com.example.docker.network

// 檔案: network/SupabaseClient.kt
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

import com.example.docker.BuildConfig

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        // 設定 JSON 序列化規則 (忽略未知的欄位，避免 App 崩潰)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            encodeDefaults = false  // 不編碼默認值
            explicitNulls = false   // 忽略 null 字段
            isLenient = true        // 寬鬆模式，容忍格式錯誤
            coerceInputValues = true // 當遇到 null 但期望非 null 時，使用默認值
        })

        install(Postgrest) // 安裝資料庫模組
        install(Auth)      // 安裝驗證模組
    }
}