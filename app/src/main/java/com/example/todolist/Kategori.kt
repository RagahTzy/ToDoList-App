package com.example.todolist

import kotlinx.serialization.Serializable

@Serializable
data class Kategori(
    val nama: String,
    val warna: Int // Argb value
)
