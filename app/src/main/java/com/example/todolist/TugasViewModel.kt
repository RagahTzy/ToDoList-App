package com.example.todolist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class TugasViewModel(context: Context) : ViewModel() { // Hapus 'private val' di sini
    private val _daftarTugas = MutableStateFlow<List<Tugas>>(emptyList())
    val daftarTugas: StateFlow<List<Tugas>> = _daftarTugas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _kategoriTugasList = MutableStateFlow<List<String>>(listOf("Individu", "Kelompok"))
    val kategoriTugasList = _kategoriTugasList.asStateFlow()

    private val _kategoriMatkulList = MutableStateFlow<List<String>>(listOf("Teori", "Praktikum"))
    val kategoriMatkulList = _kategoriMatkulList.asStateFlow()

    // Pre-calculated lists untuk performa dropdown yang instan
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val months = (1..12).map { it.toString().padStart(2, '0') }
    val years = (2025..2035).map { it.toString() }

    private val fileTugas = File(context.filesDir, "tugas.json")
    private val fileKategoriTugas = File(context.filesDir, "kategori_tugas.json")
    private val fileKategoriMatkul = File(context.filesDir, "kategori_matkul.json")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (fileTugas.exists()) {
                try {
                    val json = fileTugas.readText()
                    _daftarTugas.value = Json.decodeFromString(json)
                } catch (e: Exception) {
                    _daftarTugas.value = emptyList()
                }
            }
            if (fileKategoriTugas.exists()) {
                try {
                    val json = fileKategoriTugas.readText()
                    _kategoriTugasList.value = Json.decodeFromString(json)
                } catch (e: Exception) {}
            }
            if (fileKategoriMatkul.exists()) {
                try {
                    val json = fileKategoriMatkul.readText()
                    _kategoriMatkulList.value = Json.decodeFromString(json)
                } catch (e: Exception) {}
            }
        }
    }

    private fun saveData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fileTugas.writeText(Json.encodeToString(_daftarTugas.value))
                fileKategoriTugas.writeText(Json.encodeToString(_kategoriTugasList.value))
                fileKategoriMatkul.writeText(Json.encodeToString(_kategoriMatkulList.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun tambahTugas(tugas: Tugas) {
        _daftarTugas.value = _daftarTugas.value + tugas
        saveData()
    }

    fun hapusTugas(id: String) {
        _daftarTugas.value = _daftarTugas.value.filter { it.id != id }
        saveData()
    }

    fun updateTugas(updatedTugas: Tugas) {
        _daftarTugas.value = _daftarTugas.value.map {
            if (it.id == updatedTugas.id) updatedTugas else it
        }
        saveData()
    }

    fun tambahKategoriTugas(kategori: String) {
        if (kategori.isNotBlank() && !_kategoriTugasList.value.contains(kategori)) {
            _kategoriTugasList.value = _kategoriTugasList.value + kategori
            saveData()
        }
    }

    fun hapusKategoriTugas(kategori: String) {
        _kategoriTugasList.value = _kategoriTugasList.value.filter { it != kategori }
        saveData()
    }

    fun tambahKategoriMatkul(kategori: String) {
        if (kategori.isNotBlank() && !_kategoriMatkulList.value.contains(kategori)) {
            _kategoriMatkulList.value = _kategoriMatkulList.value + kategori
            saveData()
        }
    }

    fun hapusKategoriMatkul(kategori: String) {
        _kategoriMatkulList.value = _kategoriMatkulList.value.filter { it != kategori }
        saveData()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sortByNama() {
        _daftarTugas.value = _daftarTugas.value.sortedBy { it.namaMatkul.lowercase() }
    }

    fun sortByDeadline() {
        _daftarTugas.value = _daftarTugas.value.sortedBy { tugas ->
            val parts = tugas.deadline.split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else tugas.deadline
        }
    }

    fun sortByKategoriTugas() {
        _daftarTugas.value = _daftarTugas.value.sortedBy { it.kategoriTugas }
    }

    fun sortByKategoriMatkul() {
        _daftarTugas.value = _daftarTugas.value.sortedBy { it.kategoriMatkul }
    }
}

class TugasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TugasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TugasViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
