package com.example.todolist

import kotlinx.serialization.Serializable

@Serializable
enum class NotificationMode {
    INTERVAL,
    SPECIFIC_TIME
}

@Serializable
data class NotificationSettings(
    val mode: NotificationMode = NotificationMode.INTERVAL,
    val intervalHours: Int = 1,
    val intervalMinutes: Int = 0,
    val specificHour: Int = 16,
    val specificMinute: Int = 0
)
