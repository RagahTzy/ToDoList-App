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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: TugasViewModel by viewModels { TugasViewModelFactory(applicationContext) }

        setContent {
            ToDoListTheme(darkTheme = true) {
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    } else {
                        mutableStateOf(true)
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> hasNotificationPermission = isGranted }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!hasNotificationPermission) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    TugasApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasApp(viewModel: TugasViewModel) {
    val daftarTugas by viewModel.daftarTugas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })
    var showManageCategories by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var tugasToDelete by remember { mutableStateOf<Tugas?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filteredTugas by remember(daftarTugas, searchQuery) {
        derivedStateOf {
            val activeTugas = daftarTugas.filter { !it.isCompleted }
            if (searchQuery.isEmpty()) activeTugas
            else activeTugas.filter {
                it.namaMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriTugas.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.deadline.contains(searchQuery) ||
                        it.deskripsi.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val archivedTugas by remember(daftarTugas) {
        derivedStateOf { daftarTugas.filter { it.isCompleted } }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceDark,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "CORE SYSTEM",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                HorizontalDivider(color = NeonCyan.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("NOTIF SETTINGS", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { showNotificationSettings = true },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = NeonCyan,
                        unselectedTextColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                NavigationDrawerItem(
                    label = { Text("MANAGE CATEGORIES", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { showManageCategories = true },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
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
                            when (pagerState.currentPage) {
                                0 -> "TASK LIST"
                                1 -> "ADD TASK"
                                else -> "ARCHIVED"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = NeonCyan
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground.copy(alpha = 0.8f),
                        titleContentColor = NeonCyan,
                        navigationIconContentColor = NeonCyan,
                        actionIconContentColor = NeonCyan
                    ),
                    actions = {
                        if (pagerState.currentPage == 0) {
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Sort", tint = NeonCyan)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(SurfaceDark).border(1.dp, NeonCyan.copy(alpha = 0.3f))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Sort by Name", color = Color.White) },
                                        onClick = { viewModel.sortByNama(); showSortMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sort by Deadline", color = Color.White) },
                                        onClick = { viewModel.sortByDeadline(); showSortMenu = false },
                                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = NeonCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sort by Type", color = Color.White) },
                                        onClick = { viewModel.sortByKategoriTugas(); showSortMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = NeonCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sort by Category", color = Color.White) },
                                        onClick = { viewModel.sortByKategoriMatkul(); showSortMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = NeonCyan) }
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(40.dp)
                ) {
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Task") },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Archived", modifier = Modifier.size(20.dp)) },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> DashboardTab(
                            filteredTugas = filteredTugas,
                            searchQuery = searchQuery,
                            kategoriTugasList = kategoriTugasList,
                            kategoriMatkulList = kategoriMatkulList,
                            viewModel = viewModel,
                            onEdit = { selectedTugas = it; showEditDialog = true },
                            onDelete = { tugasToDelete = it; showDeleteConfirm = true },
                            onComplete = { viewModel.completeTugas(it.id) }
                        )
                        1 -> AddTaskTab(viewModel = viewModel)
                        2 -> ArchivedTab(
                            archivedTugas = archivedTugas,
                            kategoriTugasList = kategoriTugasList,
                            kategoriMatkulList = kategoriMatkulList,
                            onRestore = { viewModel.restoreTugas(it.id) },
                            onDelete = { tugasToDelete = it; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && tugasToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirm = false; tugasToDelete = null },
            onConfirm = { viewModel.hapusTugas(tugasToDelete!!.id); showDeleteConfirm = false; tugasToDelete = null },
            title = "DELETE TASK",
            message = "Are you sure want to delete the '${tugasToDelete!!.namaMatkul}' task? This action cannot be undone."
        )
    }

    if (showEditDialog && selectedTugas != null) {
        TugasDialog(
            viewModel = viewModel,
            tugas = selectedTugas,
            onDismiss = { showEditDialog = false },
            onConfirm = { nama, dead, katT, katM, desk ->
                viewModel.updateTugas(selectedTugas!!.copy(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk))
                showEditDialog = false
            }
        )
    }

    if (showManageCategories) {
        ManageCategoriesDialog(viewModel = viewModel, onDismiss = { showManageCategories = false })
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(viewModel = viewModel, onDismiss = { showNotificationSettings = false })
    }
}

@Composable
fun DashboardTab(
    filteredTugas: List<Tugas>,
    searchQuery: String,
    kategoriTugasList: List<Kategori>,
    kategoriMatkulList: List<Kategori>,
    viewModel: TugasViewModel,
    onEdit: (Tugas) -> Unit,
    onDelete: (Tugas) -> Unit,
    onComplete: (Tugas) -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
            placeholder = { Text("Search...", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = NeonCyan)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = NeonCyan.copy(alpha = 0.3f),
                cursorColor = NeonCyan,
                focusedContainerColor = SurfaceDark.copy(alpha = 0.5f),
                unfocusedContainerColor = SurfaceDark.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        val listState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(filteredTugas, key = { it.id }) { tugas ->
                    val typeColor = Color(kategoriTugasList.find { it.nama == tugas.kategoriTugas }?.warna ?: NeonCyan.toArgb())
                    val catColor = Color(kategoriMatkulList.find { it.nama == tugas.kategoriMatkul }?.warna ?: NeonCyan.toArgb())
                    TugasCardNeon(
                        tugas = tugas,
                        baseColor = typeColor,
                        catColor = catColor,
                        onDelete = { onDelete(tugas) },
                        onEdit = { onEdit(tugas) },
                        onToggleMute = { viewModel.toggleReminderMute(tugas.id) },
                        onComplete = { onComplete(tugas) }
                    )
                }
            }
            ScrollArrowsOverlay(
                canScrollBackward = listState.canScrollBackward,
                canScrollForward = listState.canScrollForward,
                onUpClick = { scope.launch { listState.animateScrollToItem(0) } },
                onDownClick = {
                    scope.launch {
                        if (filteredTugas.isNotEmpty()) listState.animateScrollToItem(filteredTugas.size - 1)
                    }
                }
            )
        }
    }
}

@Composable
fun AddTaskTab(viewModel: TugasViewModel) {
    AddTaskScreen(
        viewModel = viewModel,
        onSave = { nama, dead, katT, katM, desk ->
            viewModel.tambahTugas(Tugas(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk))
        }
    )
}

@Composable
fun ArchivedTab(
    archivedTugas: List<Tugas>,
    kategoriTugasList: List<Kategori>,
    kategoriMatkulList: List<Kategori>,
    onRestore: (Tugas) -> Unit,
    onDelete: (Tugas) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (archivedTugas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No archived tasks yet.", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(archivedTugas, key = { it.id }) { tugas ->
                    val typeColor = Color(kategoriTugasList.find { it.nama == tugas.kategoriTugas }?.warna ?: NeonCyan.toArgb())
                    val catColor = Color(kategoriMatkulList.find { it.nama == tugas.kategoriMatkul }?.warna ?: NeonCyan.toArgb())
                    ArchivedCard(
                        tugas = tugas,
                        baseColor = typeColor,
                        catColor = catColor,
                        onRestore = { onRestore(tugas) },
                        onDelete = { onDelete(tugas) }
                    )
                }
            }
            ScrollArrowsOverlay(
                canScrollBackward = listState.canScrollBackward,
                canScrollForward = listState.canScrollForward,
                onUpClick = { scope.launch { listState.animateScrollToItem(0) } },
                onDownClick = { scope.launch { if (archivedTugas.isNotEmpty()) listState.animateScrollToItem(archivedTugas.size - 1) } }
            )
        }
    }
}

@Composable
fun ArchivedCard(
    tugas: Tugas,
    baseColor: Color,
    catColor: Color,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, baseColor.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tugas.namaMatkul,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DEADLINE: ${tugas.deadline}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restore", tint = NeonGreen)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3366))
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeonBadge(text = tugas.kategoriTugas, color = baseColor.copy(alpha = 0.5f))
                NeonBadge(text = tugas.kategoriMatkul, color = catColor.copy(alpha = 0.5f))
            }
        }
    }
}