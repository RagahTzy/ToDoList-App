package com.example.todolist

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Catatan(
    val id: String = UUID.randomUUID().toString(),
    val tugasId: String,
    val subTugasId: String? = null,
    val judul: String,
    val isi: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)