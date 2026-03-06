package com.example.todolist

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Tugas(
    val id: String = UUID.randomUUID().toString(),
    val namaMatkul: String,
    val deadline: String,
    val kategoriTugas: String,
    val kategoriMatkul: String,
    val deskripsi: String = ""
)
