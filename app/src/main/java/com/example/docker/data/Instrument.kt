package com.example.docker.data
import kotlinx.serialization.Serializable
@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)
