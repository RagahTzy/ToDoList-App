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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit

class TugasViewModel(context: Context) : ViewModel() {
    private val _daftarTugas = MutableStateFlow<List<Tugas>>(emptyList())
    val daftarTugas = _daftarTugas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings = _notificationSettings.asStateFlow()

    private val _kategoriTugasList = MutableStateFlow(
        listOf(Kategori("Individu", NeonCyan.toArgb()), Kategori("Kelompok", NeonMagenta.toArgb()))
    )
    val kategoriTugasList = _kategoriTugasList.asStateFlow()

    private val _kategoriMatkulList = MutableStateFlow(
        listOf(Kategori("Teori", NeonCyan.toArgb()), Kategori("Praktikum", NeonPurple.toArgb()))
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

    init { loadData() }

    private inline fun <reified T> loadFromFile(file: File, default: T): T =
        if (file.exists()) try { Json.decodeFromString(file.readText()) } catch (e: Exception) { default } else default

    private fun setupRecurringNotifications(settings: NotificationSettings) {
        val totalMinutes = if (settings.mode == NotificationMode.INTERVAL)
            maxOf((settings.intervalHours * 60) + settings.intervalMinutes, 15).toLong()
        else 15L

        workManager.enqueueUniquePeriodicWork(
            "deadline_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<DeadlineWorker>(totalMinutes, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()
        )
        workManager.enqueue(OneTimeWorkRequestBuilder<DeadlineWorker>().build())
        Log.d("TugasApp", "WorkManager enqueued: mode=${settings.mode}, interval=${totalMinutes}m")
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _daftarTugas.value = loadFromFile(fileTugas, emptyList())

            val settings = loadFromFile(fileSettings, NotificationSettings())
            _notificationSettings.value = settings
            setupRecurringNotifications(settings)

            _kategoriTugasList.value = loadFromFile(fileKategoriTugas, _kategoriTugasList.value)
            _kategoriMatkulList.value = loadFromFile(fileKategoriMatkul, _kategoriMatkulList.value)
        }
    }

    private fun saveData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fileTugas.writeText(Json.encodeToString(_daftarTugas.value))
                fileKategoriTugas.writeText(Json.encodeToString(_kategoriTugasList.value))
                fileKategoriMatkul.writeText(Json.encodeToString(_kategoriMatkulList.value))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
        viewModelScope.launch(Dispatchers.IO) {
            fileSettings.writeText(Json.encodeToString(settings))
            setupRecurringNotifications(settings)
        }
    }

    fun tambahTugas(tugas: Tugas) { _daftarTugas.value += tugas; saveData() }
    fun hapusTugas(id: String) { _daftarTugas.value = _daftarTugas.value.filter { it.id != id }; saveData() }
    fun updateTugas(tugas: Tugas) { _daftarTugas.value = _daftarTugas.value.map { if (it.id == tugas.id) tugas else it }; saveData() }
    fun toggleReminderMute(id: String) { _daftarTugas.value = _daftarTugas.value.map { if (it.id == id) it.copy(reminderMuted = !it.reminderMuted) else it }; saveData() }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    private fun updateKategori(flow: MutableStateFlow<List<Kategori>>, kategori: Kategori) {
        if (kategori.nama.isNotBlank() && flow.value.none { it.nama == kategori.nama }) {
            flow.value = flow.value + kategori; saveData()
        }
    }

    private fun removeKategori(flow: MutableStateFlow<List<Kategori>>, nama: String) {
        flow.value = flow.value.filter { it.nama != nama }; saveData()
    }

    fun tambahKategoriTugas(kategori: Kategori) = updateKategori(_kategoriTugasList, kategori)
    fun hapusKategoriTugas(nama: String) = removeKategori(_kategoriTugasList, nama)
    fun tambahKategoriMatkul(kategori: Kategori) = updateKategori(_kategoriMatkulList, kategori)
    fun hapusKategoriMatkul(nama: String) = removeKategori(_kategoriMatkulList, nama)

    fun sortByNama() { _daftarTugas.value = _daftarTugas.value.sortedBy { it.namaMatkul.lowercase() } }
    fun sortByDeadline() {
        _daftarTugas.value = _daftarTugas.value.sortedBy {
            it.deadline.split("-").takeIf { p -> p.size == 3 }?.let { p -> "${p[2]}-${p[1]}-${p[0]}" } ?: it.deadline
        }
    }
    fun sortByKategoriTugas() { _daftarTugas.value = _daftarTugas.value.sortedBy { it.kategoriTugas } }
    fun sortByKategoriMatkul() { _daftarTugas.value = _daftarTugas.value.sortedBy { it.kategoriMatkul } }
}

class TugasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(TugasViewModel::class.java)) TugasViewModel(context) as T
        else throw IllegalArgumentException("Unknown ViewModel class")
}