package com.example.todolist

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.media3.common.Player
import kotlinx.coroutines.launch

// --- Mini Player ---
@Composable
fun MiniPlayer(
    lagu: Lagu?,
    isPlaying: Boolean,
    progress: Float,
    albumArt: android.graphics.Bitmap?,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lagu == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        color = SurfaceDark,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = NeonCyan,
                trackColor = NeonCyan.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (albumArt != null) {
                    Image(
                        bitmap = albumArt.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    NeonMusicIcon(size = 36.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lagu.judul,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (lagu.artis.isNotBlank()) {
                        Text(lagu.artis, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
                    }
                }
                IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// --- Full Player ---
@Composable
fun FullMusicPlayer(
    lagu: Lagu?,
    playlist: Playlist?,
    isPlaying: Boolean,
    progress: Float,
    currentPosition: Long,
    duration: Long,
    isShuffling: Boolean,
    repeatMode: Int,
    albumArt: android.graphics.Bitmap?,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onCollapse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(y = (-60).dp)
                .scale(0.95f)
                .background(
                    brush = Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent)),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(playlist?.nama ?: "", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(0.95f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, NeonCyan.copy(alpha = if (isPlaying) 0.8f else 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (albumArt != null) {
                    Image(
                        bitmap = albumArt.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, null, tint = NeonCyan.copy(alpha = 0.4f), modifier = Modifier.size(80.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    lagu?.judul ?: "No Track",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    lagu?.artis?.ifBlank { "Unknown Artist" } ?: "Unknown Artist",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(currentPosition), fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Text(formatDuration(duration), fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle, null,
                        tint = if (isShuffling) NeonCyan else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Surface(
                    modifier = Modifier.size(64.dp).clickable { onTogglePlayPause() },
                    shape = CircleShape,
                    color = NeonCyan,
                    shadowElevation = if (isPlaying) 16.dp else 4.dp
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        null,
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) NeonCyan else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// --- Main Music Tab ---
@Composable
fun MusicTab(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val playlists by viewModel.playlists.collectAsState()
    val currentLagu by viewModel.currentLagu.collectAsState()
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isShuffling by viewModel.isShuffling.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val albumArt by viewModel.currentAlbumArt.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    val miniPlayerHeight = 64.dp
    val miniPlayerVisible = currentLagu != null

    var isPlayerExpanded by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var showImportOptions by remember { mutableStateOf(false) }
    var pendingPlaylistId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importAudioFile(context, it, pendingPlaylistId) }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importPlaylistFromZip(context, it) }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let { viewModel.importFromFolder(context, it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
    }

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Loading -> snackbarHostState.showSnackbar(
                message = "Exporting playlist...",
                duration = SnackbarDuration.Indefinite
            )
            is ExportState.Success -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Export successful!")
                viewModel.resetExportState()
            }
            is ExportState.Error -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Export failed: ${(exportState as ExportState.Error).message}")
                viewModel.resetExportState()
            }
            else -> snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(importState) {
        when (importState) {
            is ImportState.Loading -> snackbarHostState.showSnackbar(
                message = "Importing playlist...",
                duration = SnackbarDuration.Indefinite
            )
            is ImportState.Success -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Import successful!")
                viewModel.resetImportState()
            }
            is ImportState.Error -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Import failed: ${(importState as ImportState.Error).message}")
                viewModel.resetImportState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceDark,
                    contentColor = Color.White,
                    actionColor = NeonCyan,
                    modifier = Modifier.padding(
                        bottom = if (miniPlayerVisible && !isPlayerExpanded) miniPlayerHeight else 0.dp
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = selectedPlaylist,
                transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
                label = "playlistNav"
            ) { playlist ->
                if (playlist == null) {
                    PlaylistListScreen(
                        playlists = playlists,
                        currentPlaylist = currentPlaylist,
                        isPlaying = isPlaying,
                        onPlaylistClick = { selectedPlaylist = it },
                        onAddPlaylist = { showAddPlaylistDialog = true },
                        onDeletePlaylist = { viewModel.hapusPlaylist(it) },
                        onExportPlaylist = { viewModel.exportPlaylistAsZip(context, it) },
                        onExportAll = { viewModel.exportAllPlaylistsAsZip(context) },
                        onImport = { showImportOptions = true },
                        onRenamePlaylist = { id, nama -> viewModel.renamePlaylist(id, nama) },
                        onImportFolder = { folderPickerLauncher.launch(null) },
                        bottomPadding = if (miniPlayerVisible && !isPlayerExpanded) miniPlayerHeight else 0.dp
                    )
                } else {
                    PlaylistDetailScreen(
                        playlist = playlists.find { it.id == playlist.id } ?: playlist,
                        currentLagu = currentLagu,
                        isPlaying = isPlaying,
                        currentPlaylistId = currentPlaylist?.id,
                        onBack = { selectedPlaylist = null },
                        onPlayLagu = { idx -> viewModel.playPlaylist(playlists.find { it.id == playlist.id } ?: playlist, idx) },
                        onAddLagu = {
                            pendingPlaylistId = playlist.id
                            audioPickerLauncher.launch("audio/*")
                        },
                        onDeleteLagu = { laguId -> viewModel.hapusLagu(playlist.id, laguId) },
                        onMoveLagu = { from, to -> viewModel.moveLagu(playlist.id, from, to) },
                        bottomPadding = if (miniPlayerVisible && !isPlayerExpanded) miniPlayerHeight else 0.dp
                    )
                }
            }

            AnimatedVisibility(
                visible = miniPlayerVisible && !isPlayerExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MiniPlayer(
                    lagu = currentLagu,
                    isPlaying = isPlaying,
                    progress = progress,
                    albumArt = albumArt,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onExpand = { isPlayerExpanded = true }
                )
            }

            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.fillMaxSize()
            ) {
                FullMusicPlayer(
                    lagu = currentLagu,
                    playlist = currentPlaylist,
                    isPlaying = isPlaying,
                    progress = progress,
                    currentPosition = currentPosition,
                    duration = duration,
                    isShuffling = isShuffling,
                    repeatMode = repeatMode,
                    albumArt = albumArt,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onSeek = viewModel::seekTo,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onToggleRepeat = viewModel::toggleRepeat,
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }

    if (showAddPlaylistDialog) {
        AddPlaylistDialog(
            onDismiss = { showAddPlaylistDialog = false },
            onConfirm = { nama -> viewModel.tambahPlaylist(nama); showAddPlaylistDialog = false }
        )
    }

    if (showImportOptions) {
        ImmersiveDialog(onDismissRequest = { showImportOptions = false }) {
            AlertDialog(
                onDismissRequest = { showImportOptions = false },
                confirmButton = {},
                title = { Text("IMPORT PLAYLIST", color = NeonCyan, fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ImportOptionButton(
                            icon = Icons.Default.FileDownload,
                            label = "From File (.taskora_playlist)",
                            onClick = { showImportOptions = false; importLauncher.launch("*/*") }
                        )
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// --- Playlist List Screen ---
@Composable
private fun PlaylistListScreen(
    playlists: List<Playlist>,
    currentPlaylist: Playlist?,
    isPlaying: Boolean,
    onPlaylistClick: (Playlist) -> Unit,
    onAddPlaylist: () -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onExportPlaylist: (Playlist) -> Unit,
    onExportAll: () -> Unit,
    onImport: () -> Unit,
    onRenamePlaylist: (id: String, nama: String) -> Unit,
    onImportFolder: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PLAYLISTS", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onImportFolder,
                            modifier = Modifier.background(NeonGreen, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.CreateNewFolder, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onImport,
                            modifier = Modifier.background(NeonPurple, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        if (playlists.isNotEmpty()) {
                            IconButton(
                                onClick = onExportAll,
                                modifier = Modifier.background(NeonMagenta, CircleShape).size(32.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(
                            onClick = onAddPlaylist,
                            modifier = Modifier.background(NeonCyan, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (playlists.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LibraryMusic, null, tint = NeonCyan.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Text("No playlists yet", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                            Text("Tap + to create one", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
                        }
                    }
                }
            }

            items(playlists, key = { it.id }) { playlist ->
                val isActive = currentPlaylist?.id == playlist.id
                var showDeleteConfirm by remember { mutableStateOf(false) }
                var showEditDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, if (isActive) NeonCyan else NeonCyan.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
                        .clickable { onPlaylistClick(playlist) },
                    colors = CardDefaults.cardColors(containerColor = if (isActive) NeonCyan.copy(alpha = 0.1f) else SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive && isPlaying) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null, tint = NeonCyan.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.nama, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${playlist.laguList.size} tracks", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                        Row {
                            IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, null, tint = NeonCyan.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onExportPlaylist(playlist) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Share, null, tint = NeonCyan.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF3366).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (showDeleteConfirm) {
                    DeleteConfirmationDialog(
                        onDismiss = { showDeleteConfirm = false },
                        onConfirm = { onDeletePlaylist(playlist.id); showDeleteConfirm = false },
                        title = "DELETE PLAYLIST",
                        message = "Delete '${playlist.nama}'? This cannot be undone."
                    )
                }
                if (showEditDialog) {
                    EditPlaylistDialog(
                        currentNama = playlist.nama,
                        onDismiss = { showEditDialog = false },
                        onConfirm = { namaBaru -> onRenamePlaylist(playlist.id, namaBaru); showEditDialog = false }
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
        ScrollArrowsOverlay(
            canScrollBackward = listState.canScrollBackward,
            canScrollForward = listState.canScrollForward,
            onUpClick = { scope.launch { listState.animateScrollToItem(0) } },
            onDownClick = { scope.launch { listState.animateScrollToItem(playlists.size) } },
            modifier = Modifier.padding(bottom = bottomPadding)
        )
    }
}

// --- Playlist Detail Screen ---
@Composable
private fun PlaylistDetailScreen(
    playlist: Playlist,
    currentLagu: Lagu?,
    isPlaying: Boolean,
    currentPlaylistId: String?,
    onBack: () -> Unit,
    onPlayLagu: (Int) -> Unit,
    onAddLagu: () -> Unit,
    onDeleteLagu: (String) -> Unit,
    onMoveLagu: (Int, Int) -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var totalDurasiText by remember { mutableStateOf("") }

    LaunchedEffect(playlist.id, playlist.laguList.size) {
        totalDurasiText = ""
        val totalMs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            var total = 0L
            playlist.laguList.forEach { lagu ->
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(lagu.localPath)
                    total += retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    retriever.release()
                } catch (e: Exception) { }
            }
            total
        }
        val totalMin = totalMs / 1000 / 60
        val totalSec = (totalMs / 1000) % 60
        totalDurasiText = if (totalMin > 0) "${totalMin} min ${totalSec} sec" else "${totalSec} sec"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NeonCyan)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(playlist.nama, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        if (totalDurasiText.isNotBlank()) {
                            Text(totalDurasiText, color = NeonCyan.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                    IconButton(
                        onClick = onAddLagu,
                        modifier = Modifier.background(NeonCyan, CircleShape).size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (playlist.laguList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MusicNote, null, tint = NeonCyan.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Text("No tracks yet", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                            Text("Tap + to add audio file", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
                        }
                    }
                }
            }

            itemsIndexed(playlist.laguList, key = { _, l -> l.id }) { index, lagu ->
                val isCurrentTrack = currentLagu?.id == lagu.id && currentPlaylistId == playlist.id
                var showDeleteConfirm by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, if (isCurrentTrack) NeonCyan else NeonCyan.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
                        .clickable { onPlayLagu(index) },
                    colors = CardDefaults.cardColors(containerColor = if (isCurrentTrack) NeonCyan.copy(alpha = 0.1f) else SurfaceDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            if (isCurrentTrack && isPlaying) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    "${index + 1}",
                                    color = if (isCurrentTrack) NeonCyan else Color.White.copy(alpha = 0.3f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lagu.judul, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (lagu.artis.isNotBlank()) {
                                Text(lagu.artis, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                        Row {
                            if (index > 0) {
                                IconButton(onClick = { onMoveLagu(index, index - 1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                            }
                            if (index < playlist.laguList.lastIndex) {
                                IconButton(onClick = { onMoveLagu(index, index + 1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF3366).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (showDeleteConfirm) {
                    DeleteConfirmationDialog(
                        onDismiss = { showDeleteConfirm = false },
                        onConfirm = { onDeleteLagu(lagu.id); showDeleteConfirm = false },
                        title = "DELETE TRACK",
                        message = "Delete '${lagu.judul}' from this playlist?"
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
        ScrollArrowsOverlay(
            canScrollBackward = listState.canScrollBackward,
            canScrollForward = listState.canScrollForward,
            onUpClick = { scope.launch { listState.animateScrollToItem(0) } },
            onDownClick = { scope.launch { listState.animateScrollToItem(playlist.laguList.size) } },
            modifier = Modifier.padding(bottom = bottomPadding)
        )
    }
}

// --- Dialogs ---
@Composable
private fun AddPlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nama by remember { mutableStateOf("") }
    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = { if (nama.isNotBlank()) onConfirm(nama) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) { Text("CREATE", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
            title = { Text("NEW PLAYLIST", color = NeonCyan, fontWeight = FontWeight.Black) },
            text = {
                OutlinedTextField(
                    value = nama, onValueChange = { nama = it },
                    label = { Text("Playlist Name", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            containerColor = SurfaceDark, shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun EditPlaylistDialog(
    currentNama: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nama by remember { mutableStateOf(currentNama) }
    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = { if (nama.isNotBlank()) onConfirm(nama) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) { Text("SAVE", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
            title = { Text("EDIT PLAYLIST", color = NeonCyan, fontWeight = FontWeight.Black) },
            text = {
                OutlinedTextField(
                    value = nama, onValueChange = { nama = it },
                    label = { Text("Playlist Name", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            containerColor = SurfaceDark, shape = RoundedCornerShape(24.dp)
        )
    }
}

// --- Helpers ---
@Composable
private fun ImportOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = SurfaceDark.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun NeonMusicIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(size / 4))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(size / 4)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.MusicNote, null, tint = NeonCyan, modifier = Modifier.size(size * 0.5f))
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}