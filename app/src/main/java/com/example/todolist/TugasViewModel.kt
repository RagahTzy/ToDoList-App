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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class TugasViewModel(private val context: Context) : ViewModel() {
    private val _daftarTugas = MutableStateFlow<List<Tugas>>(emptyList())
    val daftarTugas: StateFlow<List<Tugas>> = _daftarTugas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Pre-calculated lists untuk performa dropdown yang instan
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val months = (1..12).map { it.toString().padStart(2, '0') }
    val years = (2025..2035).map { it.toString() }

    private val file = File(context.filesDir, "tugas.json")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.exists()) {
                try {
                    val json = file.readText()
                    _daftarTugas.value = Json.decodeFromString(json)
                } catch (e: Exception) {
                    _daftarTugas.value = emptyList()
                }
            }
        }
    }

    private fun saveData() {
        val currentList = _daftarTugas.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                file.writeText(Json.encodeToString(currentList))
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
