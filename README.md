# Taskora

> Aplikasi manajemen tugas Android bertemakan **Cyberpunk/Neon**, dibangun dengan Kotlin + Jetpack Compose.

---

## Fitur Utama

- **Task Management** — Tambah, edit, hapus, dan selesaikan tugas dengan deadline
- **Sub Tugas** — Pecah tugas menjadi bagian kecil dengan progress bar
- **Catatan (Notes)** — Catatan berformat Markdown, bisa dilampirkan ke tugas atau sub tugas
- **Arsip** — Tugas selesai, tugas terhapus, catatan terhapus, kategori terhapus — semua bisa di-restore
- **Notifikasi Deadline** — Pengingat otomatis via WorkManager (mode interval atau jam tertentu)
- **Kategori Kustom** — Kelola tipe dan kategori tugas dengan warna pilihan sendiri
- **Music Player** — Putar audio lokal dengan playlist, shuffle, repeat, dan album art
- **Pencarian & Pengurutan** — Filter dan sort tugas berdasarkan nama, deadline, atau kategori
- **Markdown Guide** — Panduan lengkap penulisan Markdown tersedia dalam aplikasi

---

## Tech Stack

| Kategori | Library |
|----------|---------|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arsitektur | MVVM + StateFlow |
| Navigasi | Navigation Compose + HorizontalPager |
| Background Task | WorkManager |
| Serialisasi | kotlinx.serialization |
| Audio | Media3 (ExoPlayer + MediaSession) |
| Markdown | compose-markdown (jeziellago) |
| Min SDK | 26 (Android 8.0) |

---

## Arsitektur

Aplikasi menggunakan pola **MVVM**:

```
View (Composable)  ←→  ViewModel  ←→  File I/O (JSON)
```

- **State** diekspos sebagai `StateFlow`, dikonsumsi via `collectAsState()`
- **Persistensi** menggunakan file JSON di internal storage (tidak ada database eksternal)
- **Soft delete** diterapkan di semua entitas — tidak ada penghapusan permanen tanpa konfirmasi eksplisit

---

## Struktur Proyek

```
app/src/main/
├── java/.../todolist/
│   ├── ui.theme/          # Color, Theme, Typography
│   ├── Catatan.kt         # Data class: Catatan
│   ├── CatatanScreen.kt   # UI: Tab Notes + Detail Screen
│   ├── Colors.kt          # Palet warna Neon Cyberpunk
│   ├── CommonComponents.kt# Komponen UI bersama
│   ├── DeadlineWorker.kt  # WorkManager: cek deadline
│   ├── Kategori.kt        # Data class: Kategori
│   ├── Lagu.kt            # Data class: Lagu & Playlist
│   ├── MainActivity.kt    # Entry point + tab utama
│   ├── MusicPlayerService.kt  # MediaSessionService
│   ├── MusicScreen.kt     # UI: Music tab
│   ├── MusicViewModel.kt  # ViewModel: musik
│   ├── NotificationHelper.kt  # Helper notifikasi sistem
│   ├── NotificationSettings.kt
│   ├── Tugas.kt           # Data class: Tugas & SubTugas
│   ├── TugasCard.kt       # Komponen kartu tugas
│   ├── TugasDialog.kt     # Dialog tambah/edit tugas
│   └── TugasViewModel.kt  # ViewModel utama
└── res/
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml/
        └── file_provider_paths.xml
```

---

## Navigasi

```
NavHost
├── "main" → TugasApp
│    ├── Tab 0: Dashboard     (daftar tugas aktif)
│    ├── Tab 1: Add Task      (form tambah tugas)
│    ├── Tab 2: Notes         (catatan)
│    ├── Tab 3: Archive       (arsip semua entitas)
│    └── Tab 4: Music         (music player)
│
└── "catatan_display/{id}" → CatatanDisplayScreen
```

Navigasi antar tab menggunakan **HorizontalPager** (swipe kiri/kanan) dan bottom navigation bar. Menu drawer di kiri berisi pengaturan notifikasi, kelola kategori, dan panduan Markdown.

---

## Model Data

```kotlin
Tugas
├── id, namaMatkul, deadline ("dd-MM-yyyy")
├── kategoriTugas, kategoriMatkul
├── deskripsi (Markdown)
├── reminderMuted, isCompleted, isDeleted
└── subTugasList: List<SubTugas>

Catatan
├── id, tugasId, subTugasId (opsional)
├── judul, isi (Markdown)
├── timestamp, isDeleted

Kategori
├── nama, warna (ARGB Int), isDeleted

Playlist
├── id, nama
└── laguList: List<Lagu>
```

### Relasi

```
Tugas (1) ──── (*) SubTugas
Tugas (1) ──── (*) Catatan
SubTugas (1) ── (*) Catatan
Playlist (1) ── (*) Lagu
```

---

## Penyimpanan Data

Semua data disimpan sebagai JSON di **internal storage** (`filesDir`):

| File | Isi |
|------|-----|
| `tugas.json` | List tugas |
| `catatan.json` | List catatan |
| `kategori_tugas_v2.json` | Tipe tugas |
| `kategori_matkul_v2.json` | Kategori matkul |
| `notification_settings.json` | Pengaturan notifikasi |
| `playlists.json` | Data playlist |
| `music/` | File audio yang diimport |

---

## Notifikasi Deadline

WorkManager menjalankan `DeadlineWorker` secara periodik dengan dua mode:

- **Interval** — berjalan setiap N jam M menit (minimum 15 menit)
- **Specific Time** — berjalan setiap 15 menit, notifikasi hanya dikirim pada jam yang ditentukan (±60 menit)

Notifikasi dikirim untuk tugas dengan deadline **hari ini** (URGENT) dan **besok** (REMINDER). User bisa mematikan notifikasi per tugas via tombol mute di kartu tugas.

---

## Music Player

- Import audio dari file picker atau seluruh folder sekaligus (SAF)
- Playlist management: buat, rename, hapus, reorder lagu
- Export/import playlist sebagai file `.taskora_playlist` (ZIP)
- Background playback via `MediaSessionService` (ExoPlayer)
- Album art otomatis dari metadata audio
- Mini player + full player dengan slider, shuffle, repeat

---

## Permissions

| Permission | Kegunaan |
|-----------|---------|
| `POST_NOTIFICATIONS` | Notifikasi deadline (Android 13+) |
| `INTERNET` | Render gambar dari URL di Markdown |
| `FOREGROUND_SERVICE` | Background music |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Tipe service audio |
| `READ_MEDIA_AUDIO` | Import file audio (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Import file audio (Android ≤ 12) |

---

## Tema

Aplikasi menggunakan palet warna **Neon Cyberpunk**:

- Background: `#05050A` (hampir hitam)
- Surface: `#121224`
- Accent utama: Cyan `#00F2FF`, Magenta `#FF00D4`, Purple `#9D00FF`
- Status indikator: Green `#39FF14` (selesai), Red `#FF3131` (deadline dekat)

---