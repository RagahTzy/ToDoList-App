package com.example.todolist

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Lagu(
    val id: String = UUID.randomUUID().toString(),
    val judul: String,
    val artis: String = "",
    val localPath: String = "", // path di internal storage app
    val fileName: String = ""  // nama file asli, untuk zip export
)

@Serializable
data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val laguList: List<Lagu> = emptyList()
)