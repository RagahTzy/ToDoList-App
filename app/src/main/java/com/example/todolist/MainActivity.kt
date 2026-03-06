package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.launch

// Neon Cyberpunk Palette
val NeonCyan = Color(0xFF00F2FF)
val NeonMagenta = Color(0xFFFF00D4)
val NeonPurple = Color(0xFF9D00FF)
val DarkBackground = Color(0xFF05050A)
val SurfaceDark = Color(0xFF121224)

// Efek Neon Glow Custom
fun Modifier.neonGlow(
    color: Color,
    borderRadius: Dp = 16.dp,
    glowRadius: Dp = 8.dp,
    alpha: Float = 0.5f
) = this.drawBehind {
    val paint = Paint().asFrameworkPaint().apply {
        this.color = color.copy(alpha = alpha).toArgb()
        this.setShadowLayer(glowRadius.toPx(), 0f, 0f, color.toArgb())
    }
    drawIntoCanvas {
        it.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            borderRadius.toPx(), borderRadius.toPx(),
            paint
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: TugasViewModel by viewModels { TugasViewModelFactory(applicationContext) }

        setContent {
            ToDoListTheme(darkTheme = true) {
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

    var showDialog by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // State konfirmasi hapus
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
                    label = { Text("MANAGE CATEGORIES", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        showManageCategories = true
                    },
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
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.1f), Color.Transparent),
                        radius = 1500f
                    )
                )) {
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

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(filteredTugas, key = { it.id }) { tugas ->
                            TugasCardNeon(
                                tugas = tugas,
                                onDelete = { 
                                    tugasToDelete = tugas
                                    showDeleteConfirm = true
                                },
                                onEdit = {
                                    selectedTugas = tugas
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && tugasToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { 
                showDeleteConfirm = false
                tugasToDelete = null
            },
            onConfirm = {
                viewModel.hapusTugas(tugasToDelete!!.id)
                showDeleteConfirm = false
                tugasToDelete = null
            },
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
        ManageCategoriesDialog(
            viewModel = viewModel,
            onDismiss = { showManageCategories = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TugasCardNeon(tugas: Tugas, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isKelompok = tugas.kategoriTugas.contains("Group", ignoreCase = true)
    val baseColor = if (isKelompok) NeonMagenta else NeonCyan
    
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "borderTransition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "borderAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .neonGlow(baseColor, alpha = 0.2f * alpha)
            .border(
                BorderStroke(1.dp, Brush.linearGradient(listOf(baseColor.copy(alpha = alpha), Color.Transparent))),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tugas.namaMatkul,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DEADLINE: ${tugas.deadline}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3366))
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val badgeColor = if (isKelompok) Color.White else NeonMagenta
                    NeonBadge(text = tugas.kategoriTugas, color = badgeColor)
                    NeonBadge(text = tugas.kategoriMatkul, color = NeonCyan)
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = baseColor,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = baseColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = baseColor,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (tugas.deskripsi.isNotBlank()) tugas.deskripsi else "No description provided.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NeonBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasDialog(
    viewModel: TugasViewModel,
    tugas: Tugas?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var namaMatkul by remember { mutableStateOf(tugas?.namaMatkul ?: "") }
    var deskripsi by remember { mutableStateOf(tugas?.deskripsi ?: "") }
    val initialDate = tugas?.deadline?.split("-") ?: listOf("", "", "")
    var selectedDay by remember { mutableStateOf(initialDate.getOrNull(0) ?: "01") }
    var selectedMonth by remember { mutableStateOf(initialDate.getOrNull(1) ?: "01") }
    var selectedYear by remember { mutableStateOf(initialDate.getOrNull(2) ?: "2025") }
    
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    var kategoriTugas by remember { mutableStateOf(tugas?.kategoriTugas ?: if (kategoriTugasList.isNotEmpty()) kategoriTugasList[0] else "") }
    var kategoriMatkul by remember { mutableStateOf(tugas?.kategoriMatkul ?: if (kategoriMatkulList.isNotEmpty()) kategoriMatkulList[0] else "") }

    var showKategoriTugasSelector by remember { mutableStateOf(false) }
    var showKategoriMatkulSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(namaMatkul, "$selectedDay-$selectedMonth-$selectedYear", kategoriTugas, kategoriMatkul, deskripsi) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.neonGlow(NeonCyan, borderRadius = 12.dp, glowRadius = 6.dp)
            ) {
                Text("SAVE TASK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        },
        title = {
            Text(
                text = if (tugas == null) "ADD TASK" else "EDIT TASK",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = namaMatkul,
                    onValueChange = { namaMatkul = it },
                    label = { Text("Subject Name", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Description", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEADLINE (Day/Month/Year)", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SimpleDropdown(options = viewModel.days, selected = selectedDay, onSelected = { selectedDay = it }, modifier = Modifier.weight(1f))
                        SimpleDropdown(options = viewModel.months, selected = selectedMonth, onSelected = { selectedMonth = it }, modifier = Modifier.weight(1f))
                        SimpleDropdown(options = viewModel.years, selected = selectedYear, onSelected = { selectedYear = it }, modifier = Modifier.weight(1.2f))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    
                    // Button to select Kategori Tugas
                    CategoryButton(
                        label = "Type: $kategoriTugas",
                        color = NeonMagenta,
                        onClick = { showKategoriTugasSelector = true }
                    )

                    // Button to select Kategori Matkul
                    CategoryButton(
                        label = "Category: $kategoriMatkul",
                        color = NeonCyan,
                        onClick = { showKategoriMatkulSelector = true }
                    )
                }
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .padding(16.dp)
            .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), RoundedCornerShape(28.dp))
    )

    if (showKategoriTugasSelector) {
        CategorySelectionDialog(
            title = "SELECT TYPE",
            options = kategoriTugasList,
            onSelected = { kategoriTugas = it; showKategoriTugasSelector = false },
            onDismiss = { showKategoriTugasSelector = false }
        )
    }

    if (showKategoriMatkulSelector) {
        CategorySelectionDialog(
            title = "SELECT CATEGORY",
            options = kategoriMatkulList,
            onSelected = { kategoriMatkul = it; showKategoriMatkulSelector = false },
            onDismiss = { showKategoriMatkulSelector = false }
        )
    }
}

@Composable
fun CategoryButton(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun CategorySelectionDialog(
    title: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(title, color = NeonCyan, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) },
                        color = SurfaceDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ManageCategoriesDialog(
    viewModel: TugasViewModel,
    onDismiss: () -> Unit
) {
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()
    
    var newKategoriTugas by remember { mutableStateOf("") }
    var newKategoriMatkul by remember { mutableStateOf("") }

    // Konfirmasi Hapus Kategori
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf("") } // "Tugas" atau "Matkul"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = NeonCyan) }
        },
        title = { Text("MANAGE CATEGORIES", color = NeonCyan, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section Kategori Tugas
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TASK TYPES (Individu/Kelompok)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newKategoriTugas,
                            onValueChange = { newKategoriTugas = it },
                            placeholder = { Text("New Type", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(
                            onClick = { viewModel.tambahKategoriTugas(newKategoriTugas); newKategoriTugas = "" },
                            modifier = Modifier.background(NeonMagenta, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        }
                    }
                    kategoriTugasList.forEach { kat ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(kat, color = Color.White.copy(alpha = 0.8f))
                            IconButton(onClick = { 
                                categoryToDelete = kat
                                deleteType = "Task"
                                showDeleteConfirm = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Section Kategori Matkul
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TASK CATEGORIES (Theory/Practicum)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newKategoriMatkul,
                            onValueChange = { newKategoriMatkul = it },
                            placeholder = { Text("New Category", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(
                            onClick = { viewModel.tambahKategoriMatkul(newKategoriMatkul); newKategoriMatkul = "" },
                            modifier = Modifier.background(NeonCyan, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        }
                    }
                    kategoriMatkulList.forEach { kat ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(kat, color = Color.White.copy(alpha = 0.8f))
                            IconButton(onClick = { 
                                categoryToDelete = kat
                                deleteType = "Category"
                                showDeleteConfirm = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp)
    )

    if (showDeleteConfirm && categoryToDelete != null) {
        val dialogTitle = if (deleteType == "Task") "DELETE TASK TYPE" else "DELETE TASK CATEGORY"
        val dialogMessage = if (deleteType == "Task") {
            "Are you sure you want to delete the '$categoryToDelete' task type? This action cannot be undone."
        } else {
            "Are you sure you want to delete the '$categoryToDelete' category? This action cannot be undone."
        }

        DeleteConfirmationDialog(
            onDismiss = {
                showDeleteConfirm = false
                categoryToDelete = null
            },
            onConfirm = {
                if (deleteType == "Task") {
                    viewModel.hapusKategoriTugas(categoryToDelete!!)
                } else {
                    viewModel.hapusKategoriMatkul(categoryToDelete!!)
                }
                showDeleteConfirm = false
                categoryToDelete = null
            },
            title = dialogTitle,
            message = dialogMessage
        )
    }
}

@Composable
fun SimpleDropdown(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            color = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, color = Color.White, fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp).background(SurfaceDark).border(1.dp, NeonCyan.copy(alpha = 0.3f))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.neonGlow(Color(0xFFFF3366), borderRadius = 12.dp, glowRadius = 6.dp)
            ) {
                Text("DELETE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF3366),
                letterSpacing = 2.sp
            )
        },
        text = {
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .padding(16.dp)
            .border(BorderStroke(1.dp, Color(0xFFFF3366).copy(alpha = 0.5f)), RoundedCornerShape(28.dp))
    )
}
