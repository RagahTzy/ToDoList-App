package com.example.todolist

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit

class TugasViewModel(context: Context) : ViewModel() {
    private val _daftarTugas = MutableStateFlow<List<Tugas>>(emptyList())
    val daftarTugas: StateFlow<List<Tugas>> = _daftarTugas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings = _notificationSettings.asStateFlow()

    private val _kategoriTugasList = MutableStateFlow<List<Kategori>>(
        listOf(
            Kategori("Individu", NeonCyan.toArgb()),
            Kategori("Kelompok", NeonMagenta.toArgb())
        )
    )
    val kategoriTugasList = _kategoriTugasList.asStateFlow()

    private val _kategoriMatkulList = MutableStateFlow<List<Kategori>>(
        listOf(
            Kategori("Teori", NeonCyan.toArgb()),
            Kategori("Praktikum", NeonPurple.toArgb())
        )
    )
    val kategoriMatkulList = _kategoriMatkulList.asStateFlow()

    val days = (1..31).map { it.toString().padStart(2, '0') }
    val months = (1..12).map { it.toString().padStart(2, '0') }
    val years = (2025..2035).map { it.toString() }

    private val fileTugas = File(context.filesDir, "tugas.json")
    private val fileKategoriTugas = File(context.filesDir, "kategori_tugas_v2.json")
    private val fileKategoriMatkul = File(context.filesDir, "kategori_matkul_v2.json")
    private val fileSettings = File(context.filesDir, "notification_settings.json")
    
    private val workManager = WorkManager.getInstance(context)

    init {
        loadData()
    }

    private fun setupRecurringNotifications(settings: NotificationSettings) {
        Log.d("TugasApp", "Setting up notifications: Mode=${settings.mode}")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val totalMinutes = if (settings.mode == NotificationMode.INTERVAL) {
            (settings.intervalHours * 60) + settings.intervalMinutes
        } else {
            15 // Cek tiap 15 menit untuk SPECIFIC_TIME
        }

        val workRequest = PeriodicWorkRequestBuilder<DeadlineWorker>(
            maxOf(totalMinutes.toLong(), 15L), 
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "deadline_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE, 
            workRequest
        )

        // Pemicu instan untuk testing
        val testRequest = OneTimeWorkRequestBuilder<DeadlineWorker>().build()
        workManager.enqueue(testRequest)
        
        Log.d("TugasApp", "WorkManager enqueued with interval: $totalMinutes minutes")
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
            if (fileSettings.exists()) {
                try {
                    val json = fileSettings.readText()
                    val settings = Json.decodeFromString<NotificationSettings>(json)
                    _notificationSettings.value = settings
                    setupRecurringNotifications(settings)
                } catch (e: Exception) {
                    setupRecurringNotifications(NotificationSettings())
                }
            } else {
                setupRecurringNotifications(NotificationSettings())
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

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
        viewModelScope.launch(Dispatchers.IO) {
            fileSettings.writeText(Json.encodeToString(settings))
            setupRecurringNotifications(settings)
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

    fun toggleReminderMute(id: String) {
        _daftarTugas.value = _daftarTugas.value.map {
            if (it.id == id) it.copy(reminderMuted = !it.reminderMuted) else it
        }
        saveData()
    }

    fun tambahKategoriTugas(kategori: Kategori) {
        if (kategori.nama.isNotBlank() && _kategoriTugasList.value.none { it.nama == kategori.nama }) {
            _kategoriTugasList.value = _kategoriTugasList.value + kategori
            saveData()
        }
    }

    fun hapusKategoriTugas(nama: String) {
        _kategoriTugasList.value = _kategoriTugasList.value.filter { it.nama != nama }
        saveData()
    }

    fun tambahKategoriMatkul(kategori: Kategori) {
        if (kategori.nama.isNotBlank() && _kategoriMatkulList.value.none { it.nama == kategori.nama }) {
            _kategoriMatkulList.value = _kategoriMatkulList.value + kategori
            saveData()
        }
    }

    fun hapusKategoriMatkul(nama: String) {
        _kategoriMatkulList.value = _kategoriMatkulList.value.filter { it.nama != nama }
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
