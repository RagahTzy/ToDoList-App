package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredTugas by remember(daftarTugas, searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) daftarTugas
            else daftarTugas.filter {
                it.namaMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriTugas.contains(searchQuery, ignoreCase = true) ||
                        it.kategoriMatkul.contains(searchQuery, ignoreCase = true) ||
                        it.deadline.contains(searchQuery)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HOMEWORK LIST",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        color = NeonCyan
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground.copy(alpha = 0.8f)
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
                                text = { Text("Urut Nama", color = Color.White) },
                                onClick = { viewModel.sortByNama(); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Urut Deadline", color = Color.White) },
                                onClick = { viewModel.sortByDeadline(); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = NeonCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Urut Kategori", color = Color.White) },
                                onClick = { viewModel.sortByKategoriTugas(); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = NeonCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Urut Matkul", color = Color.White) },
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
                targetValue = if (isPressed) 0.9f else 1f,
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = NeonCyan.copy(alpha = 0.3f),
                        cursorColor = NeonCyan,
                        containerColor = SurfaceDark.copy(alpha = 0.5f)
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
                            onDelete = { viewModel.hapusTugas(tugas.id) },
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

    if (showDialog) {
        TugasDialog(
            viewModel = viewModel,
            tugas = selectedTugas,
            onDismiss = { showDialog = false },
            onConfirm = { nama, dead, katT, katM ->
                if (selectedTugas == null) {
                    viewModel.tambahTugas(Tugas(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM))
                } else {
                    viewModel.updateTugas(selectedTugas!!.copy(namaMatkul = nama, deadline = dead, kategoriTugas = katT, kategoriMatkul = katM))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun TugasCardNeon(tugas: Tugas, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isKelompok = tugas.kategoriTugas.contains("Kelompok", ignoreCase = true)
    val baseColor = if (isKelompok) NeonMagenta else NeonCyan
    
    val infiniteTransition = rememberInfiniteTransition(label = "")
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
            .neonGlow(baseColor, alpha = 0.2f * alpha)
            .border(
                BorderStroke(1.dp, Brush.linearGradient(listOf(baseColor.copy(alpha = alpha), Color.Transparent))),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    val badgeColor = if (isKelompok) Color.White else NeonMagenta
                    NeonBadge(text = tugas.kategoriTugas, color = badgeColor)
                    NeonBadge(text = tugas.kategoriMatkul, color = NeonCyan)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF3366))
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
    onConfirm: (String, String, String, String) -> Unit
) {
    var namaMatkul by remember { mutableStateOf(tugas?.namaMatkul ?: "") }
    val initialDate = tugas?.deadline?.split("-") ?: listOf("", "", "")
    var selectedDay by remember { mutableStateOf(initialDate.getOrNull(0) ?: "01") }
    var selectedMonth by remember { mutableStateOf(initialDate.getOrNull(1) ?: "01") }
    var selectedYear by remember { mutableStateOf(initialDate.getOrNull(2) ?: "2025") }
    var kategoriTugas by remember { mutableStateOf(tugas?.kategoriTugas ?: "Individu") }
    var kategoriMatkul by remember { mutableStateOf(tugas?.kategoriMatkul ?: "Teori") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), RoundedCornerShape(28.dp))
            .background(SurfaceDark),
        content = {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (tugas == null) "ADD TASK" else "EDIT TASK",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )

                OutlinedTextField(
                    value = namaMatkul,
                    onValueChange = { namaMatkul = it },
                    label = { Text("Subject Name", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEADLINE", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SimpleDropdown(options = viewModel.days, selected = selectedDay, onSelected = { selectedDay = it }, modifier = Modifier.weight(1f))
                        SimpleDropdown(options = viewModel.months, selected = selectedMonth, onSelected = { selectedMonth = it }, modifier = Modifier.weight(1f))
                        SimpleDropdown(options = viewModel.years, selected = selectedYear, onSelected = { selectedYear = it }, modifier = Modifier.weight(1.2f))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Individu", "Kelompok").forEach {
                            FilterChip(
                                selected = kategoriTugas == it,
                                onClick = { kategoriTugas = it },
                                label = { Text(it) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonMagenta,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = kategoriTugas == it,
                                    borderColor = Color.Gray,
                                    selectedBorderColor = NeonMagenta,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Teori", "Praktikum").forEach {
                            FilterChip(
                                selected = kategoriMatkul == it,
                                onClick = { kategoriMatkul = it },
                                label = { Text(it) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = kategoriMatkul == it,
                                    borderColor = Color.Gray,
                                    selectedBorderColor = NeonCyan,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(namaMatkul, "$selectedDay-$selectedMonth-$selectedYear", kategoriTugas, kategoriMatkul) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.neonGlow(NeonCyan, borderRadius = 12.dp, glowRadius = 6.dp)
                    ) {
                        Text("SAVE TASK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
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
