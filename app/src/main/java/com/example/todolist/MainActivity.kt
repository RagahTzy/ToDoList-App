package com.example.todolist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TugasViewModel by viewModels { TugasViewModelFactory(applicationContext) }
        setContent {
            ToDoListTheme(darkTheme = true) {
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    } else mutableStateOf(true)
                }
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasNotificationPermission = it }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission)
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: TugasViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            TugasApp(viewModel = viewModel, onNavigateToCatatan = { navController.navigate("catatan_display/$it") })
        }
        composable("catatan_display/{catatanId}") { backStackEntry ->
            val catatanId = backStackEntry.arguments?.getString("catatanId") ?: return@composable
            CatatanDisplayScreen(catatanId = catatanId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasApp(viewModel: TugasViewModel, onNavigateToCatatan: (String) -> Unit) {
    val daftarTugas by viewModel.daftarTugas.collectAsState()
    val daftarCatatan by viewModel.daftarCatatan.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 4 })
    var showManageCategories by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var tugasToDelete by remember { mutableStateOf<Tugas?>(null) }
    var showMarkdownGuide by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val filteredTugas by remember(daftarTugas, searchQuery) {
        derivedStateOf {
            val active = daftarTugas.filter { !it.isCompleted && !it.isDeleted }
            if (searchQuery.isEmpty()) active
            else active.filter {
                it.namaMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriTugas.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.deadline.contains(searchQuery) ||
                        it.deskripsi.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val completedTugas by remember(daftarTugas) { derivedStateOf { daftarTugas.filter { it.isCompleted && !it.isDeleted } } }
    val deletedTugas by remember(daftarTugas) { derivedStateOf { daftarTugas.filter { it.isDeleted } } }
    val deletedCatatan by remember(daftarCatatan) { derivedStateOf { daftarCatatan.filter { it.isDeleted } } }
    val deletedKategoriTugas by remember(kategoriTugasList) { derivedStateOf { kategoriTugasList.filter { it.isDeleted } } }
    val deletedKategoriMatkul by remember(kategoriMatkulList) { derivedStateOf { kategoriMatkulList.filter { it.isDeleted } } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceDark,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text("CORE SYSTEM", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                HorizontalDivider(color = NeonCyan.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("NOTIF SETTINGS", fontWeight = FontWeight.Bold) },
                    selected = false, onClick = { showNotificationSettings = true },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedIconColor = NeonCyan, unselectedTextColor = Color.White),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                NavigationDrawerItem(
                    label = { Text("MANAGE CATEGORIES", fontWeight = FontWeight.Bold) },
                    selected = false, onClick = { showManageCategories = true },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedIconColor = NeonCyan, unselectedTextColor = Color.White),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                NavigationDrawerItem(
                    label = { Text("MARKDOWN GUIDE", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { showMarkdownGuide = true },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = NeonCyan,
                        unselectedTextColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        Text(
                            when (pagerState.currentPage) { 0 -> "TASK LIST"; 1 -> "ADD TASK"; 2 -> "NOTES"; else -> "ARCHIVE" },
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, letterSpacing = 4.sp, color = NeonCyan
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground.copy(alpha = 0.8f), titleContentColor = NeonCyan, navigationIconContentColor = NeonCyan, actionIconContentColor = NeonCyan),
                    actions = {
                        if (pagerState.currentPage == 0) {
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Sort", tint = NeonCyan)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu, onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(SurfaceDark).border(1.dp, NeonCyan.copy(alpha = 0.3f))
                                ) {
                                    DropdownMenuItem(text = { Text("Sort by Name", color = Color.White) }, onClick = { viewModel.sortByNama(); showSortMenu = false }, leadingIcon = { Icon(Icons.Default.Info, null, tint = NeonCyan) })
                                    DropdownMenuItem(text = { Text("Sort by Deadline", color = Color.White) }, onClick = { viewModel.sortByDeadline(); showSortMenu = false }, leadingIcon = { Icon(Icons.Default.DateRange, null, tint = NeonCyan) })
                                    DropdownMenuItem(text = { Text("Sort by Type", color = Color.White) }, onClick = { viewModel.sortByKategoriTugas(); showSortMenu = false }, leadingIcon = { Icon(Icons.Default.Favorite, null, tint = NeonCyan) })
                                    DropdownMenuItem(text = { Text("Sort by Category", color = Color.White) }, onClick = { viewModel.sortByKategoriMatkul(); showSortMenu = false }, leadingIcon = { Icon(Icons.Default.Star, null, tint = NeonCyan) })
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.Black, tonalElevation = 0.dp, modifier = Modifier.height(40.dp)) {
                    NavigationBarItem(selected = pagerState.currentPage == 0, onClick = { scope.launch { pagerState.animateScrollToPage(0) } }, icon = { Icon(Icons.Default.Home, "Dashboard") }, label = null, alwaysShowLabel = false, colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, indicatorColor = Color.Transparent, unselectedIconColor = Color.White.copy(alpha = 0.4f)))
                    NavigationBarItem(selected = pagerState.currentPage == 1, onClick = { scope.launch { pagerState.animateScrollToPage(1) } }, icon = { Icon(Icons.Default.AddCircle, "Add Task") }, label = null, alwaysShowLabel = false, colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, indicatorColor = Color.Transparent, unselectedIconColor = Color.White.copy(alpha = 0.4f)))
                    NavigationBarItem(selected = pagerState.currentPage == 2, onClick = { scope.launch { pagerState.animateScrollToPage(2) } }, icon = { Icon(Icons.AutoMirrored.Filled.Notes, "Notes", modifier = Modifier.size(22.dp)) }, label = null, alwaysShowLabel = false, colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, indicatorColor = Color.Transparent, unselectedIconColor = Color.White.copy(alpha = 0.4f)))
                    NavigationBarItem(selected = pagerState.currentPage == 3, onClick = { scope.launch { pagerState.animateScrollToPage(3) } }, icon = { Icon(Icons.Default.Folder, "Archive", modifier = Modifier.size(20.dp)) }, label = null, alwaysShowLabel = false, colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonCyan, indicatorColor = Color.Transparent, unselectedIconColor = Color.White.copy(alpha = 0.4f)))
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            ) {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> DashboardTab(filteredTugas, searchQuery, kategoriTugasList, kategoriMatkulList, daftarCatatan.filter { !it.isDeleted }, viewModel, onEdit = { selectedTugas = it; showEditDialog = true }, onDelete = { tugasToDelete = it; showDeleteConfirm = true }, onComplete = { viewModel.completeTugas(it.id) }, onCatatanClick = onNavigateToCatatan)
                        1 -> AddTaskTab(viewModel)
                        2 -> CatatanTab(viewModel = viewModel, onNavigateToDisplay = onNavigateToCatatan)
                        3 -> ArchivedTab(
                            completedTugas = completedTugas,
                            deletedTugas = deletedTugas,
                            deletedCatatan = deletedCatatan,
                            deletedKategoriTugas = deletedKategoriTugas,
                            deletedKategoriMatkul = deletedKategoriMatkul,
                            kategoriTugasList = kategoriTugasList,
                            kategoriMatkulList = kategoriMatkulList,
                            daftarTugas = daftarTugas,
                            viewModel = viewModel,
                            onNavigateToCatatan = onNavigateToCatatan
                        )
                    }
                }
            }
        }
    }

    // Delete task dialog (soft delete)
    if (showDeleteConfirm && tugasToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirm = false; tugasToDelete = null },
            onConfirm = { viewModel.hapusTugas(tugasToDelete!!.id); showDeleteConfirm = false; tugasToDelete = null },
            title = "DELETE TASK",
            message = "Are you sure you want to delete '${tugasToDelete!!.namaMatkul}'? It will be moved to the archive. Related notes will not be permanently deleted."
        )
    }

    if (showEditDialog && selectedTugas != null) {
        TugasDialog(
            viewModel = viewModel, tugas = selectedTugas, onDismiss = { showEditDialog = false },
            onConfirm = { nama, dead, katT, katM, desk, subList ->
                viewModel.updateTugas(selectedTugas!!.copy(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk, subTugasList = subList))
                showEditDialog = false
            }
        )
    }

    if (showManageCategories) ManageCategoriesDialog(viewModel = viewModel, onDismiss = { showManageCategories = false })
    if (showNotificationSettings) NotificationSettingsDialog(viewModel = viewModel, onDismiss = { showNotificationSettings = false })
    if (showMarkdownGuide) MarkdownGuideDialog(onDismiss = { showMarkdownGuide = false })
}

@Composable
fun DashboardTab(
    filteredTugas: List<Tugas>, searchQuery: String,
    kategoriTugasList: List<Kategori>, kategoriMatkulList: List<Kategori>,
    daftarCatatan: List<Catatan>, viewModel: TugasViewModel,
    onEdit: (Tugas) -> Unit, onDelete: (Tugas) -> Unit,
    onComplete: (Tugas) -> Unit, onCatatanClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery, onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
            placeholder = { Text("Search...", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = NeonCyan) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Close, null, tint = NeonCyan) } },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = NeonCyan.copy(alpha = 0.3f), cursorColor = NeonCyan, focusedContainerColor = SurfaceDark.copy(alpha = 0.5f), unfocusedContainerColor = SurfaceDark.copy(alpha = 0.5f), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            singleLine = true
        )
        val listState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(filteredTugas, key = { it.id }) { tugas ->
                    val typeColor = Color(kategoriTugasList.find { it.nama == tugas.kategoriTugas }?.warna ?: NeonCyan.toArgb())
                    val catColor = Color(kategoriMatkulList.find { it.nama == tugas.kategoriMatkul }?.warna ?: NeonCyan.toArgb())
                    TugasCardNeon(tugas = tugas, baseColor = typeColor, catColor = catColor, catatanList = daftarCatatan.filter { it.tugasId == tugas.id }, onDelete = { onDelete(tugas) }, onEdit = { onEdit(tugas) }, onToggleMute = { viewModel.toggleReminderMute(tugas.id) }, onComplete = { onComplete(tugas) }, onToggleSubTugas = { viewModel.toggleSubTugas(tugas.id, it) }, onCatatanClick = onCatatanClick)
                }
            }
            ScrollArrowsOverlay(canScrollBackward = listState.canScrollBackward, canScrollForward = listState.canScrollForward, onUpClick = { scope.launch { listState.animateScrollToItem(0) } }, onDownClick = { scope.launch { if (filteredTugas.isNotEmpty()) listState.animateScrollToItem(filteredTugas.size - 1) } })
        }
    }
}

@Composable
fun AddTaskTab(viewModel: TugasViewModel) {
    AddTaskScreen(viewModel = viewModel, onSave = { nama, dead, katT, katM, desk, subList ->
        viewModel.tambahTugas(Tugas(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk, subTugasList = subList))
    })
}

@Composable
fun ArchivedTab(
    completedTugas: List<Tugas>,
    deletedTugas: List<Tugas>,
    deletedCatatan: List<Catatan>,
    deletedKategoriTugas: List<Kategori>,
    deletedKategoriMatkul: List<Kategori>,
    kategoriTugasList: List<Kategori>,
    kategoriMatkulList: List<Kategori>,
    daftarTugas: List<Tugas>,
    viewModel: TugasViewModel,
    onNavigateToCatatan: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // State for confirm dialogs
    var showDeleteConfirmArchive by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var onConfirmAction by remember { mutableStateOf({}) }

    fun showDeleteDialog(title: String, message: String, action: () -> Unit) {
        confirmTitle = title; confirmMessage = message; onConfirmAction = action; showDeleteConfirmArchive = true
    }
    fun showRestoreDialog(title: String, message: String, action: () -> Unit) {
        confirmTitle = title; confirmMessage = message; onConfirmAction = action; showRestoreConfirm = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // --- COMPLETED TASKS ---
            item {
                ArchiveSectionHeader(title = "COMPLETED TASKS", color = NeonGreen, count = completedTugas.size)
            }
            if (completedTugas.isEmpty()) {
                item { ArchiveEmptyText("No completed tasks.") }
            } else {
                items(completedTugas, key = { "completed_${it.id}" }) { tugas ->
                    val typeColor = Color(kategoriTugasList.find { it.nama == tugas.kategoriTugas }?.warna ?: NeonCyan.toArgb())
                    val catColor = Color(kategoriMatkulList.find { it.nama == tugas.kategoriMatkul }?.warna ?: NeonCyan.toArgb())
                    ArchivedTaskCard(
                        tugas = tugas, baseColor = typeColor, catColor = catColor,
                        onRestore = {
                            showRestoreDialog("RESTORE TASK", "Restore '${tugas.namaMatkul}' to active tasks?") { viewModel.restoreTugas(tugas.id) }
                        },
                        onDelete = {
                            showDeleteDialog("DELETE TASK", "Permanently delete '${tugas.namaMatkul}'? This action cannot be undone.") { viewModel.hapusTugasPermanen(tugas.id) }
                        }
                    )
                }
            }

            item { HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp)) }

            // --- DELETED TASKS ---
            item { ArchiveSectionHeader(title = "DELETED TASKS", color = NeonRed, count = deletedTugas.size) }
            if (deletedTugas.isEmpty()) {
                item { ArchiveEmptyText("No deleted tasks.") }
            } else {
                items(deletedTugas, key = { "deleted_${it.id}" }) { tugas ->
                    val typeColor = Color(kategoriTugasList.find { it.nama == tugas.kategoriTugas }?.warna ?: NeonCyan.toArgb())
                    val catColor = Color(kategoriMatkulList.find { it.nama == tugas.kategoriMatkul }?.warna ?: NeonCyan.toArgb())
                    ArchivedTaskCard(
                        tugas = tugas, baseColor = typeColor, catColor = catColor,
                        onRestore = {
                            showRestoreDialog("RESTORE TASK", "Restore '${tugas.namaMatkul}' to active tasks?") { viewModel.restoreDeletedTugas(tugas.id) }
                        },
                        onDelete = {
                            showDeleteDialog("PERMANENTLY DELETE TASK", "Permanently delete '${tugas.namaMatkul}'? This action cannot be undone. Related notes will not be permanently deleted.") { viewModel.hapusTugasPermanen(tugas.id) }
                        }
                    )
                }
            }

            item { HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp)) }

            // --- DELETED NOTES ---
            item { ArchiveSectionHeader(title = "DELETED NOTES", color = NeonMagenta, count = deletedCatatan.size) }
            if (deletedCatatan.isEmpty()) {
                item { ArchiveEmptyText("No deleted notes.") }
            } else {
                items(deletedCatatan, key = { "catatan_${it.id}" }) { catatan ->
                    val tugasNama = daftarTugas.find { it.id == catatan.tugasId }?.namaMatkul ?: "Unknown Task"
                    val subTugasNama = catatan.subTugasId?.let { subId -> daftarTugas.find { it.id == catatan.tugasId }?.subTugasList?.find { it.id == subId }?.nama }
                    val tugasIsDeleted = daftarTugas.find { it.id == catatan.tugasId }?.isDeleted ?: true
                    val tugasExists = daftarTugas.any { it.id == catatan.tugasId }
                    DeletedNoteCard(
                        catatan = catatan,
                        tugasNama = tugasNama,
                        subTugasNama = subTugasNama,
                        canRestore = tugasExists && !tugasIsDeleted,
                        onRestore = {
                            showRestoreDialog("RESTORE NOTE", "Restore the note '${catatan.judul}'?") { viewModel.restoreDeletedCatatan(catatan.id) }
                        },
                        onDelete = {
                            showDeleteDialog("PERMANENTLY DELETE NOTE", "Permanently delete the note '${catatan.judul}'? This action cannot be undone.") { viewModel.hapusCatatanPermanen(catatan.id) }
                        }
                    )
                }
            }

            item { HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp)) }

            // --- DELETED CATEGORIES ---
            val allDeletedKategori = deletedKategoriTugas.map { it to "TYPE" } + deletedKategoriMatkul.map { it to "CATEGORY" }
            item { ArchiveSectionHeader(title = "DELETED CATEGORIES", color = NeonYellow, count = allDeletedKategori.size) }
            if (allDeletedKategori.isEmpty()) {
                item { ArchiveEmptyText("No deleted categories.") }
            } else {
                items(allDeletedKategori, key = { "kat_${it.second}_${it.first.nama}" }) { (kategori, type) ->
                    DeletedKategoriCard(
                        kategori = kategori,
                        type = type,
                        onRestore = {
                            showRestoreDialog("RESTORE ${type}", "Restore the ${type.lowercase()} '${kategori.nama}'?") {
                                if (type == "TYPE") viewModel.restoreKategoriTugas(kategori.nama)
                                else viewModel.restoreKategoriMatkul(kategori.nama)
                            }
                        },
                        onDelete = {
                            showDeleteDialog("PERMANENTLY DELETE ${type}", "Permanently delete the ${type.lowercase()} '${kategori.nama}'? This action cannot be undone.") {
                                if (type == "TYPE") viewModel.hapusKategoriTugasPermanen(kategori.nama)
                                else viewModel.hapusKategoriMatkulPermanen(kategori.nama)
                            }
                        }
                    )
                }
            }
        }

        ScrollArrowsOverlay(
            canScrollBackward = listState.canScrollBackward, canScrollForward = listState.canScrollForward,
            onUpClick = { scope.launch { listState.animateScrollToItem(0) } },
            onDownClick = { scope.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) } }
        )
    }

    if (showDeleteConfirmArchive) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirmArchive = false },
            onConfirm = { onConfirmAction(); showDeleteConfirmArchive = false },
            title = confirmTitle,
            message = confirmMessage
        )
    }

    if (showRestoreConfirm) {
        RestoreConfirmationDialog(
            onDismiss = { showRestoreConfirm = false },
            onConfirm = { onConfirmAction(); showRestoreConfirm = false },
            title = confirmTitle,
            message = confirmMessage
        )
    }
}

@Composable
private fun ArchiveSectionHeader(title: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Surface(color = color.copy(alpha = 0.2f), shape = CircleShape) {
            Text("$count", fontSize = 10.sp, color = color, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun ArchiveEmptyText(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
}

@Composable
fun ArchivedTaskCard(tugas: Tugas, baseColor: Color, catColor: Color, onRestore: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, baseColor.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tugas.namaMatkul, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("DEADLINE: ${tugas.deadline}", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
                Row {
                    IconButton(onClick = onRestore) { Icon(Icons.Default.Refresh, "Restore", tint = NeonGreen) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF3366)) }
                }
            }
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonBadge(text = tugas.kategoriTugas, color = baseColor.copy(alpha = 0.5f))
                NeonBadge(text = tugas.kategoriMatkul, color = catColor.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun DeletedNoteCard(
    catatan: Catatan,
    tugasNama: String,
    subTugasNama: String?,
    canRestore: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(catatan.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(catatan.timestamp))
    }
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NeonMagenta.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(catatan.judul, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (subTugasNama != null) "$tugasNama › $subTugasNama" else tugasNama,
                    color = NeonMagenta.copy(alpha = 0.5f), fontSize = 11.sp
                )
                Text(dateStr, color = Color.Gray, fontSize = 10.sp)
                if (!canRestore) {
                    Text("Restore the parent task first to restore this note.", color = NeonYellow.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
            Row {
                if (canRestore) {
                    IconButton(onClick = onRestore) { Icon(Icons.Default.Refresh, "Restore", tint = NeonGreen) }
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF3366)) }
            }
        }
    }
}

@Composable
fun DeletedKategoriCard(kategori: Kategori, type: String, onRestore: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color(kategori.warna).copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(kategori.warna).copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(10.dp).background(Color(kategori.warna), CircleShape))
                Column {
                    Text(kategori.nama, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(type, color = Color(kategori.warna).copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            Row {
                IconButton(onClick = onRestore) { Icon(Icons.Default.Refresh, "Restore", tint = NeonGreen) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF3366)) }
            }
        }
    }
}