package com.example.todolist

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist = _currentPlaylist.asStateFlow()

    private val _currentLagu = MutableStateFlow<Lagu?>(null)
    val currentLagu = _currentLagu.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _isShuffling = MutableStateFlow(false)
    val isShuffling = _isShuffling.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val filePlaylist = File(getApplication<Application>().filesDir, "playlists.json")
    private val musicDir = File(getApplication<Application>().filesDir, "music").also { it.mkdirs() }

    private val _currentAlbumArt = MutableStateFlow<android.graphics.Bitmap?>(null)
    val currentAlbumArt = _currentAlbumArt.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState = _exportState.asStateFlow()

    fun resetExportState() { _exportState.value = ExportState.Idle }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun resetImportState() { _importState.value = ImportState.Idle }

    init {
        loadPlaylists()
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicPlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
            startProgressTracking()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val idx = controller?.currentMediaItemIndex ?: return
            val lagu = _currentPlaylist.value?.laguList?.getOrNull(idx)
            _currentLagu.value = lagu
            _duration.value = controller?.duration?.takeIf { it > 0 } ?: 0L
            lagu?.let { loadAlbumArt(it.localPath) } // tambah ini
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY)
                _duration.value = controller?.duration?.takeIf { it > 0 } ?: 0L
        }
    }

    private fun startProgressTracking() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                val c = controller ?: continue
                val dur = c.duration.takeIf { it > 0 } ?: continue
                _currentPosition.value = c.currentPosition
                _duration.value = dur
                _progress.value = c.currentPosition.toFloat() / dur.toFloat()
            }
        }
    }

    // --- Playback ---
    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        _currentPlaylist.value = playlist
        val mediaItems = playlist.laguList.map { MediaItem.fromUri(Uri.fromFile(File(it.localPath))) }
        controller?.run {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
        val lagu = playlist.laguList.getOrNull(startIndex)
        _currentLagu.value = lagu
        lagu?.let { loadAlbumArt(it.localPath) } // tambah ini
    }

    fun togglePlayPause() { controller?.run { if (isPlaying) pause() else play() } }
    fun next() = controller?.seekToNextMediaItem()
    fun previous() = controller?.seekToPreviousMediaItem()

    fun seekTo(fraction: Float) {
        val dur = _duration.value
        if (dur > 0) controller?.seekTo((fraction * dur).toLong())
    }

    fun toggleShuffle() {
        _isShuffling.value = !_isShuffling.value
        controller?.shuffleModeEnabled = _isShuffling.value
    }

    fun toggleRepeat() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        controller?.repeatMode = next
    }

    // --- Import audio file dari storage ---
    fun importAudioFile(context: Context, uri: Uri, playlistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val judul = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: "Unknown"
                val artis = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                retriever.release()

                val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment?.substringAfterLast("/") ?: "audio"}"
                val destFile = File(musicDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                tambahLagu(playlistId, Lagu(judul = judul, artis = artis, localPath = destFile.absolutePath, fileName = fileName))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Playlist CRUD ---
    fun tambahPlaylist(nama: String) { _playlists.value += Playlist(nama = nama); savePlaylists() }

    fun importFromFolder(context: Context, folderUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val docUri = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, folderUri) ?: return@launch
                val folderName = docUri.name ?: "New Playlist"

                val audioFiles = docUri.listFiles().filter { file ->
                    file.isFile && file.type?.startsWith("audio/") == true
                }

                if (audioFiles.isEmpty()) return@launch

                val laguList = audioFiles.mapNotNull { file ->
                    try {
                        val retriever = android.media.MediaMetadataRetriever()
                        retriever.setDataSource(context, file.uri)
                        val judul = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                            ?: file.name?.substringBeforeLast(".") ?: "Unknown"
                        val artis = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                        retriever.release()

                        val fileName = "${System.currentTimeMillis()}_${file.name ?: "audio"}"
                        val destFile = File(musicDir, fileName)
                        context.contentResolver.openInputStream(file.uri)?.use { input ->
                            FileOutputStream(destFile).use { output -> input.copyTo(output) }
                        }

                        Lagu(judul = judul, artis = artis, localPath = destFile.absolutePath, fileName = fileName)
                    } catch (e: Exception) { null }
                }

                if (laguList.isEmpty()) return@launch

                _playlists.value += Playlist(nama = folderName, laguList = laguList)
                savePlaylists()
                _importState.value = ImportState.Success
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
                e.printStackTrace()
            }
        }
    }

    fun hapusPlaylist(id: String) {
        val playlist = _playlists.value.find { it.id == id }
        playlist?.laguList?.forEach { File(it.localPath).delete() }
        _playlists.value = _playlists.value.filter { it.id != id }
        if (_currentPlaylist.value?.id == id) {
            controller?.stop()
            _currentPlaylist.value = null
            _currentLagu.value = null
        }
        savePlaylists()
    }

    fun renamePlaylist(id: String, namaBaru: String) {
        _playlists.value = _playlists.value.map {
            if (it.id == id) it.copy(nama = namaBaru) else it
        }
        if (_currentPlaylist.value?.id == id)
            _currentPlaylist.value = _currentPlaylist.value?.copy(nama = namaBaru)
        savePlaylists()
    }

    fun tambahLagu(playlistId: String, lagu: Lagu) {
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) it.copy(laguList = it.laguList + lagu) else it
        }
        if (_currentPlaylist.value?.id == playlistId)
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        savePlaylists()
    }

    fun hapusLagu(playlistId: String, laguId: String) {
        val lagu = _playlists.value.find { it.id == playlistId }?.laguList?.find { it.id == laguId }
        lagu?.let { File(it.localPath).delete() }
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) it.copy(laguList = it.laguList.filter { l -> l.id != laguId }) else it
        }
        if (_currentPlaylist.value?.id == playlistId)
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        savePlaylists()
    }

    fun moveLagu(playlistId: String, fromIndex: Int, toIndex: Int) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id != playlistId) return@map playlist
            val list = playlist.laguList.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            playlist.copy(laguList = list)
        }
        savePlaylists()
    }

    // --- Export ZIP ---
    fun exportPlaylistAsZip(context: Context, playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            _exportState.value = ExportState.Loading
            try {
                val cacheDir = File(context.cacheDir, "playlists").also { it.mkdirs() }
                val zipFile = File(cacheDir, "${playlist.nama.replace(" ", "_")}.taskora_playlist")

                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    zos.putNextEntry(ZipEntry("metadata.json"))
                    zos.write(Json.encodeToString(playlist).toByteArray())
                    zos.closeEntry()
                    playlist.laguList.forEach { lagu ->
                        val audioFile = File(lagu.localPath)
                        if (audioFile.exists()) {
                            zos.putNextEntry(ZipEntry("audio/${lagu.fileName}"))
                            audioFile.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }

                shareFile(context, zipFile)
                _exportState.value = ExportState.Success
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportAllPlaylistsAsZip(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _exportState.value = ExportState.Loading  // tambah
            try {
                val cacheDir = File(context.cacheDir, "playlists").also { it.mkdirs() }
                val zipFile = File(cacheDir, "all_playlists.taskora_playlists")

                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    zos.putNextEntry(ZipEntry("metadata.json"))
                    zos.write(Json.encodeToString(_playlists.value).toByteArray())
                    zos.closeEntry()
                    _playlists.value.forEach { playlist ->
                        playlist.laguList.forEach { lagu ->
                            val audioFile = File(lagu.localPath)
                            if (audioFile.exists()) {
                                zos.putNextEntry(ZipEntry("audio/${playlist.id}/${lagu.fileName}"))
                                audioFile.inputStream().use { it.copyTo(zos) }
                                zos.closeEntry()
                            }
                        }
                    }
                }

                shareFile(context, zipFile)
                _exportState.value = ExportState.Success  // tambah
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")  // tambah
            }
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Playlist").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // --- Import ZIP ---
    fun importPlaylistFromZip(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val isSingle = uri.lastPathSegment?.endsWith(".taskora_playlist") == true

                // Tahap 1: ekstrak metadata dulu, audio langsung tulis ke disk
                var metadataJson = ""
                val audioFileMap = mutableMapOf<String, File>() // zipEntryName -> destFile

                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == "metadata.json" -> {
                                metadataJson = zis.readBytes().toString(Charsets.UTF_8)
                            }
                            entry.name.startsWith("audio/") -> {
                                // Langsung stream ke file, tidak buffer ke memory
                                val fileName = "${System.currentTimeMillis()}_${entry.name.substringAfterLast("/")}"
                                val destFile = File(musicDir, fileName)
                                FileOutputStream(destFile).use { out -> zis.copyTo(out) }
                                audioFileMap[entry.name] = destFile
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                if (metadataJson.isEmpty()) return@launch

                // Tahap 2: parse metadata dan mapping ke file yang sudah ditulis
                if (isSingle) {
                    val playlist = Json.decodeFromString<Playlist>(metadataJson)
                    val newLaguList = playlist.laguList.map { lagu ->
                        val destFile = audioFileMap["audio/${lagu.fileName}"]
                        if (destFile != null) {
                            lagu.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                localPath = destFile.absolutePath,
                                fileName = destFile.name
                            )
                        } else lagu
                    }
                    _playlists.value += playlist.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        laguList = newLaguList
                    )
                } else {
                    val importedPlaylists = Json.decodeFromString<List<Playlist>>(metadataJson)
                    val newPlaylists = importedPlaylists.map { playlist ->
                        val newLaguList = playlist.laguList.map { lagu ->
                            val destFile = audioFileMap["audio/${playlist.id}/${lagu.fileName}"]
                            if (destFile != null) {
                                lagu.copy(
                                    id = java.util.UUID.randomUUID().toString(),
                                    localPath = destFile.absolutePath,
                                    fileName = destFile.name
                                )
                            } else lagu
                        }
                        playlist.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            laguList = newLaguList
                        )
                    }
                    _playlists.value += newPlaylists
                }

                savePlaylists()
                _importState.value = ImportState.Success
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
                e.printStackTrace()
            }
        }
    }

    private fun loadAlbumArt(localPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(localPath)
                val bytes = retriever.embeddedPicture
                retriever.release()
                _currentAlbumArt.value = bytes?.let {
                    android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                }
            } catch (e: Exception) {
                _currentAlbumArt.value = null
            }
        }
    }

    // --- Persist ---
    private fun loadPlaylists() {
        if (filePlaylist.exists()) {
            try { _playlists.value = Json.decodeFromString(filePlaylist.readText()) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun savePlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try { filePlaylist.writeText(Json.encodeToString(_playlists.value)) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture ?: return)
        super.onCleared()
    }
}