# Taskora

> An Android task management application with a **Cyberpunk/Neon** theme, built using Kotlin + Jetpack Compose.

---

## Main Features

* **Task Management** — Add, edit, delete, and complete tasks with deadlines
* **Subtasks** — Break tasks into smaller parts with a progress bar
* **Notes** — Markdown-formatted notes that can be attached to tasks or subtasks
* **Archive** — Completed tasks, deleted tasks, deleted notes, and deleted categories — all can be restored
* **Deadline Notifications** — Automatic reminders via WorkManager (interval mode or specific time)
* **Custom Categories** — Manage task types and categories with your own color choices
* **Music Player** — Play local audio with playlists, shuffle, repeat, and album art
* **Search & Sorting** — Filter and sort tasks by name, deadline, or category
* **Markdown Guide** — A complete Markdown writing guide available داخل the app

---

## Tech Stack

| Category        | Library                              |
| --------------- | ------------------------------------ |
| Language        | Kotlin                               |
| UI              | Jetpack Compose + Material 3         |
| Architecture    | MVVM + StateFlow                     |
| Navigation      | Navigation Compose + HorizontalPager |
| Background Task | WorkManager                          |
| Serialization   | kotlinx.serialization                |
| Audio           | Media3 (ExoPlayer + MediaSession)    |
| Markdown        | compose-markdown (jeziellago)        |
| Min SDK         | 26 (Android 8.0)                     |

---

## Architecture

The application uses the **MVVM** pattern:

```
View (Composable)  ←→  ViewModel  ←→  File I/O (JSON)
```

* **State** is exposed as `StateFlow`, consumed via `collectAsState()`
* **Persistence** uses JSON files in internal storage (no external database)
* **Soft delete** is applied to all entities — no permanent deletion without explicit confirmation

---

## Project Structure

```
app/src/main/
├── java/.../todolist/
│   ├── ui.theme/          # Color, Theme, Typography
│   ├── Catatan.kt         # Data class: Note
│   ├── CatatanScreen.kt   # UI: Notes tab + Detail Screen
│   ├── Colors.kt          # Neon Cyberpunk color palette
│   ├── CommonComponents.kt# Shared UI components
│   ├── DeadlineWorker.kt  # WorkManager: deadline checker
│   ├── Kategori.kt        # Data class: Category
│   ├── Lagu.kt            # Data class: Song & Playlist
│   ├── MainActivity.kt    # Entry point + main tabs
│   ├── MusicPlayerService.kt  # MediaSessionService
│   ├── MusicScreen.kt     # UI: Music tab
│   ├── MusicViewModel.kt  # ViewModel: music
│   ├── NotificationHelper.kt  # System notification helper
│   ├── NotificationSettings.kt
│   ├── Tugas.kt           # Data class: Task & Subtask
│   ├── TugasCard.kt       # Task card component
│   ├── TugasDialog.kt     # Add/edit task dialog
│   └── TugasViewModel.kt  # Main ViewModel
└── res/
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml/
        └── file_provider_paths.xml
```

---

## Navigation

```
NavHost
├── "main" → TugasApp
│    ├── Tab 0: Dashboard     (active task list)
│    ├── Tab 1: Add Task      (task creation form)
│    ├── Tab 2: Notes         (notes)
│    ├── Tab 3: Archive       (all archived entities)
│    └── Tab 4: Music         (music player)
│
└── "catatan_display/{id}" → CatatanDisplayScreen
```

Navigation between tabs uses **HorizontalPager** (swipe left/right) and a bottom navigation bar. The left drawer menu contains notification settings, category management, and a Markdown guide.

---

## Data Model

```kotlin
Task
├── id, subjectName, deadline ("dd-MM-yyyy")
├── taskCategory, subjectCategory
├── description (Markdown)
├── reminderMuted, isCompleted, isDeleted
└── subTaskList: List<SubTask>

Note
├── id, taskId, subTaskId (optional)
├── title, content (Markdown)
├── timestamp, isDeleted

Category
├── name, color (ARGB Int), isDeleted

Playlist
├── id, name
└── songList: List<Song>
```

### Relationships

```
Task (1) ──── (*) SubTask  
Task (1) ──── (*) Note  
SubTask (1) ── (*) Note  
Playlist (1) ── (*) Song  
```

---

## Data Storage

All data is stored as JSON in **internal storage** (`filesDir`):

| File                         | Content               |
| ---------------------------- | --------------------- |
| `tugas.json`                 | Task list             |
| `catatan.json`               | Note list             |
| `kategori_tugas_v2.json`     | Task types            |
| `kategori_matkul_v2.json`    | Subject categories    |
| `notification_settings.json` | Notification settings |
| `playlists.json`             | Playlist data         |
| `music/`                     | Imported audio files  |

---

## Deadline Notifications

WorkManager runs `DeadlineWorker` periodically in two modes:

* **Interval** — runs every N hours M minutes (minimum 15 minutes)
* **Specific Time** — runs every 15 minutes, notifications are only sent at specified times (±60 minutes tolerance)

Notifications are sent for tasks with deadlines **today** (URGENT) and **tomorrow** (REMINDER). Users can mute notifications per task via a mute button on the task card.

---

## Music Player

* Import audio from file picker or entire folders (SAF)
* Playlist management: create, rename, delete, reorder songs
* Export/import playlists as `.taskora_playlist` files (ZIP)
* Background playback via `MediaSessionService` (ExoPlayer)
* Automatic album art from audio metadata
* Mini player + full player with slider, shuffle, repeat

---

## Permissions

| Permission                          | Purpose                              |
| ----------------------------------- | ------------------------------------ |
| `POST_NOTIFICATIONS`                | Deadline notifications (Android 13+) |
| `INTERNET`                          | Render images from URLs in Markdown  |
| `FOREGROUND_SERVICE`                | Background music                     |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Audio service type                   |
| `READ_MEDIA_AUDIO`                  | Import audio files (Android 13+)     |
| `READ_EXTERNAL_STORAGE`             | Import audio files (Android ≤ 12)    |

---

## Theme

The app uses a **Neon Cyberpunk** color palette:

* Background: `#05050A` (almost black)
* Surface: `#121224`
* Main accents: Cyan `#00F2FF`, Magenta `#FF00D4`, Purple `#9D00FF`
* Status indicators: Green `#39FF14` (completed), Red `#FF3131` (near deadline)

---
