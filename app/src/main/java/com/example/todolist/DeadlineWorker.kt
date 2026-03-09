package com.example.todolist

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DeadlineWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): androidx.work.ListenableWorker.Result {
        Log.d("DeadlineWorker", "!!! WORKER STARTING CHECK !!!")
        val fileTugas = File(applicationContext.filesDir, "tugas.json")
        val fileSettings = File(applicationContext.filesDir, "notification_settings.json")
        
        if (!fileTugas.exists()) {
            Log.d("DeadlineWorker", "tugas.json not found")
            return androidx.work.ListenableWorker.Result.success()
        }

        try {
            val jsonTugas = fileTugas.readText()
            val tasks: List<Tugas> = Json.decodeFromString(jsonTugas)
            Log.d("DeadlineWorker", "Found ${tasks.size} tasks in database")
            
            val settings = if (fileSettings.exists()) {
                Json.decodeFromString<NotificationSettings>(fileSettings.readText())
            } else {
                NotificationSettings()
            }

            // SPECIFIC_TIME mode check
            if (settings.mode == NotificationMode.SPECIFIC_TIME) {
                val now = java.time.LocalTime.now()
                val targetTime = java.time.LocalTime.of(settings.specificHour, settings.specificMinute)
                val diff = java.time.Duration.between(targetTime, now).toMinutes()
                
                Log.d("DeadlineWorker", "Mode: SPECIFIC_TIME. Target: $targetTime, Now: $now, Diff: $diff mins")
                
                if (diff < 0 || diff > 60) {
                    Log.d("DeadlineWorker", "Skipping: Not the specific time yet.")
                    return androidx.work.ListenableWorker.Result.success()
                }
            } else {
                Log.d("DeadlineWorker", "Mode: INTERVAL (${settings.intervalHours}h ${settings.intervalMinutes}m)")
            }
            
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val notificationHelper = NotificationHelper(applicationContext)

            tasks.forEach { tugas ->
                if (!tugas.reminderMuted) {
                    try {
                        val deadlineDate = LocalDate.parse(tugas.deadline, formatter)
                        val daysUntil = ChronoUnit.DAYS.between(today, deadlineDate)
                        Log.d("DeadlineWorker", "Checking Task: ${tugas.namaMatkul} | Deadline: ${tugas.deadline} | DaysUntil: $daysUntil")
                        
                        if (daysUntil == 1L || daysUntil == 0L) {
                            val prefix = if (daysUntil == 0L) "URGENT" else "REMINDER"
                            val msg = if (daysUntil == 0L) "Deadline hari ini!" else "Deadline besok!"
                            
                            Log.d("DeadlineWorker", ">>> TRIGGERING NOTIFICATION for ${tugas.namaMatkul} (ID: ${tugas.id}) <<<")
                            notificationHelper.showNotification(
                                tugas.id, // Gunakan ID unik tugas
                                "$prefix: ${tugas.namaMatkul}",
                                "$msg Jangan lupa dikumpulkan!"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("DeadlineWorker", "Error parsing date for ${tugas.namaMatkul}: ${tugas.deadline}")
                    }
                } else {
                    Log.d("DeadlineWorker", "Task ${tugas.namaMatkul} is muted, skipping notification.")
                }
            }
        } catch (e: Exception) {
            Log.e("DeadlineWorker", "CRITICAL ERROR in Worker", e)
            return androidx.work.ListenableWorker.Result.failure()
        }

        return androidx.work.ListenableWorker.Result.success()
    }
}
