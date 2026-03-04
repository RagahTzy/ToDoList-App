package com.example.todolist

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    val mahasiswaList = mutableStateListOf<Mahasiswa>()

    fun tambahMahasiswa(mahasiswa: Mahasiswa) {
        mahasiswaList.add(mahasiswa)
    }

    fun hapusMahasiswa(index: Int) {
        if (index in mahasiswaList.indices) {
            mahasiswaList.removeAt(index)
        }
    }

    fun updateMahasiswa(index: Int, mahasiswa: Mahasiswa) {
        if (index in mahasiswaList.indices) {
            mahasiswaList[index] = mahasiswa
        }
    }

    fun sortByNim() {
        mahasiswaList.sortBy { it.nim }
    }

    fun sortByNama() {
        mahasiswaList.sortBy { it.nama }
    }

    fun sortByJurusan() {
        mahasiswaList.sortBy { it.jurusan }
    }

    // Simpan ke file JSON
    fun simpanKeFile(filename: String = "data_mahasiswa.json") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().filesDir, filename)
                val json = Json.encodeToString(mahasiswaList)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Load dari file JSON
    fun loadDariFile(filename: String = "data_mahasiswa.json") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().filesDir, filename)
                if (file.exists()) {
                    val json = file.readText()
                    val list: List<Mahasiswa> = Json.decodeFromString(json)
                    mahasiswaList.clear()
                    mahasiswaList.addAll(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}