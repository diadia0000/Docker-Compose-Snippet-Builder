package com.example.docker.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EnvVarConverter {
    private val gson = Gson()

    /**
     * Converts a raw string of environment variables (key=value per line)
     * into a JSON string for database storage.
     */
    fun rawStringToJson(raw: String): String {
        val map = mutableMapOf<String, String>()
        raw.lines().forEach { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                if (key.isNotEmpty()) {
                    map[key] = value
                }
            }
        }
        return gson.toJson(map)
    }

    /**
     * Converts a JSON string from database back to a raw string
     * (key=value per line) for UI display.
     */
    fun jsonToRawString(json: String): String {
        val map = jsonToMap(json)
        return map.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    /**
     * Parses a JSON string into a Map<String, String>.
     */
    fun jsonToMap(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Formats a Map<String, String> into a YAML environment block.
     * Indentation should be handled by the caller or this function.
     * Here we return a list of strings formatted as "- KEY=VALUE".
     */
    fun mapToYamlList(map: Map<String, String>): List<String> {
        return map.map { "${it.key}=${it.value}" }
    }
}

