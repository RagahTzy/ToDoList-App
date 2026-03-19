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
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemBars(window)

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
                    color = DarkBackground
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

    var showDialog by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var tugasToDelete by remember { mutableStateOf<Tugas?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filteredTugas by remember(daftarTugas, searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) daftarTugas
            else daftarTugas.filter {
                it.namaMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriTugas.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.deadline.contains(searchQuery) ||
                        it.deskripsi.contains(searchQuery, ignoreCase = true)
            }
        }
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
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        Text(
                            "TASK LIST",
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
                )
            },
            floatingActionButton = {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "fabScale"
                )
                FloatingActionButton(
                    onClick = { selectedTugas = null; showDialog = true },
                    interactionSource = interactionSource,
                    containerColor = NeonCyan,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .scale(scale)
                        .neonGlow(NeonCyan, borderRadius = 28.dp, glowRadius = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.1f), Color.Transparent),
                            radius = 1500f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .neonGlow(NeonCyan, borderRadius = 24.dp, glowRadius = 4.dp, alpha = 0.1f),
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
                                    onDelete = { tugasToDelete = tugas; showDeleteConfirm = true },
                                    onEdit = { selectedTugas = tugas; showDialog = true },
                                    onToggleMute = { viewModel.toggleReminderMute(tugas.id) }
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

    if (showDialog) {
        TugasDialog(
            viewModel = viewModel,
            tugas = selectedTugas,
            onDismiss = { showDialog = false },
            onConfirm = { nama, dead, katT, katM, desk ->
                if (selectedTugas == null) {
                    viewModel.tambahTugas(Tugas(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk))
                } else {
                    viewModel.updateTugas(selectedTugas!!.copy(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM, deskripsi = desk))
                }
                showDialog = false
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