package com.example.docker.data
import kotlin.serialization.Serializable
@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)
