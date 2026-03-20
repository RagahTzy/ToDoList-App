package com.example.todolist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
private fun TaskFormFields(
    namaMatkul: String, onNamaChange: (String) -> Unit,
    deskripsi: String, onDeskripsiChange: (String) -> Unit,
    selectedDay: String, selectedMonth: String, selectedYear: String,
    onDayChange: (String) -> Unit, onMonthChange: (String) -> Unit, onYearChange: (String) -> Unit,
    kategoriTugas: String, kategoriMatkul: String,
    currentTypeColor: Color, currentCatColor: Color,
    onTypeClick: () -> Unit, onCategoryClick: () -> Unit,
    viewModel: TugasViewModel,
    subTugasList: List<SubTugas>,
    onAddSubTugas: (String) -> Unit,
    onRemoveSubTugas: (String) -> Unit
) {
    var newSubTugasNama by remember { mutableStateOf("") }

    OutlinedTextField(
        value = namaMatkul, onValueChange = onNamaChange,
        label = { Text("Subject Name", color = NeonCyan.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, cursorColor = NeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = deskripsi, onValueChange = onDeskripsiChange,
        label = { Text("Description", color = NeonCyan.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, cursorColor = NeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
        shape = RoundedCornerShape(12.dp), minLines = 3
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("DEADLINE (Day/Month/Year)", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SimpleDropdown(viewModel.days, selectedDay, onDayChange, Modifier.weight(1f))
            SimpleDropdown(viewModel.months, selectedMonth, onMonthChange, Modifier.weight(1f))
            SimpleDropdown(viewModel.years, selectedYear, onYearChange, Modifier.weight(1.2f))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
        CategoryButton("Type: $kategoriTugas", currentTypeColor, onTypeClick)
        CategoryButton("Category: $kategoriMatkul", currentCatColor, onCategoryClick)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SUB TASKS", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubTugasNama, onValueChange = { newSubTugasNama = it },
                placeholder = { Text("Add sub task...", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, cursorColor = NeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )
            IconButton(
                onClick = { if (newSubTugasNama.isNotBlank()) { onAddSubTugas(newSubTugasNama); newSubTugasNama = "" } },
                modifier = Modifier.background(NeonCyan, CircleShape)
            ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black) }
        }
        subTugasList.forEach { sub ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(sub.nama, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveSubTugas(sub.id) }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TugasDialog(
    viewModel: TugasViewModel, tugas: Tugas?, onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, List<SubTugas>) -> Unit
) {
    var namaMatkul by remember { mutableStateOf(tugas?.namaMatkul ?: "") }
    var deskripsi by remember { mutableStateOf(tugas?.deskripsi ?: "") }
    val dateParts = tugas?.deadline?.split("-")
    var selectedDay by remember { mutableStateOf(dateParts?.getOrNull(0) ?: "01") }
    var selectedMonth by remember { mutableStateOf(dateParts?.getOrNull(1) ?: "01") }
    var selectedYear by remember { mutableStateOf(dateParts?.getOrNull(2) ?: "2025") }

    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    // Filter hanya yang tidak deleted
    val activeKategoriTugas = kategoriTugasList.filter { !it.isDeleted }
    val activeKategoriMatkul = kategoriMatkulList.filter { !it.isDeleted }

    var kategoriTugas by remember { mutableStateOf(tugas?.kategoriTugas ?: activeKategoriTugas.firstOrNull()?.nama ?: "") }
    var kategoriMatkul by remember { mutableStateOf(tugas?.kategoriMatkul ?: activeKategoriMatkul.firstOrNull()?.nama ?: "") }
    var subTugasList by remember { mutableStateOf(tugas?.subTugasList ?: emptyList()) }

    var showKategoriTugasSelector by remember { mutableStateOf(false) }
    var showKategoriMatkulSelector by remember { mutableStateOf(false) }

    val currentTypeColor = Color(activeKategoriTugas.find { it.nama == kategoriTugas }?.warna ?: kategoriTugasList.find { it.nama == kategoriTugas }?.warna ?: NeonCyan.toArgb())
    val currentCatColor = Color(activeKategoriMatkul.find { it.nama == kategoriMatkul }?.warna ?: kategoriMatkulList.find { it.nama == kategoriMatkul }?.warna ?: NeonCyan.toArgb())
    val scope = rememberCoroutineScope()

    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = { onConfirm(namaMatkul, "$selectedDay-$selectedMonth-$selectedYear", kategoriTugas, kategoriMatkul, deskripsi, subTugasList) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SAVE TASK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
            title = {
                Text(
                    text = if (tugas == null) "ADD TASK" else "EDIT TASK",
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = NeonCyan, letterSpacing = 2.sp
                )
            },
            text = {
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        TaskFormFields(
                            namaMatkul = namaMatkul, onNamaChange = { namaMatkul = it },
                            deskripsi = deskripsi, onDeskripsiChange = { deskripsi = it },
                            selectedDay = selectedDay, selectedMonth = selectedMonth, selectedYear = selectedYear,
                            onDayChange = { selectedDay = it }, onMonthChange = { selectedMonth = it }, onYearChange = { selectedYear = it },
                            kategoriTugas = kategoriTugas, kategoriMatkul = kategoriMatkul,
                            currentTypeColor = currentTypeColor, currentCatColor = currentCatColor,
                            onTypeClick = { showKategoriTugasSelector = true }, onCategoryClick = { showKategoriMatkulSelector = true },
                            viewModel = viewModel, subTugasList = subTugasList,
                            onAddSubTugas = { subTugasList = subTugasList + SubTugas(nama = it) },
                            onRemoveSubTugas = { id -> subTugasList = subTugasList.filter { it.id != id } }
                        )
                    }
                    ScrollArrowsOverlay(canScrollBackward = scrollState.canScrollBackward, canScrollForward = scrollState.canScrollForward, onUpClick = { scope.launch { scrollState.animateScrollTo(0) } }, onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } })
                }
            },
            containerColor = SurfaceDark, shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(16.dp).border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), RoundedCornerShape(28.dp))
        )
    }

    if (showKategoriTugasSelector) {
        CategorySelectionDialog("SELECT TYPE", activeKategoriTugas, onSelected = { kategoriTugas = it.nama; showKategoriTugasSelector = false }, onDismiss = { showKategoriTugasSelector = false })
    }
    if (showKategoriMatkulSelector) {
        CategorySelectionDialog("SELECT CATEGORY", activeKategoriMatkul, onSelected = { kategoriMatkul = it.nama; showKategoriMatkulSelector = false }, onDismiss = { showKategoriMatkulSelector = false })
    }
}

@Composable
fun CategoryButton(label: String, color: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, color = SurfaceDark, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun CategorySelectionDialog(title: String, options: List<Kategori>, onSelected: (Kategori) -> Unit, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss, confirmButton = {},
            title = { Text(title, color = NeonCyan, fontWeight = FontWeight.Black) },
            text = {
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { option ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onSelected(option) },
                                color = SurfaceDark.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(option.warna).copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.size(12.dp).background(Color(option.warna), CircleShape))
                                    Text(option.nama, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    ScrollArrowsOverlay(canScrollBackward = scrollState.canScrollBackward, canScrollForward = scrollState.canScrollForward, onUpClick = { scope.launch { scrollState.animateScrollTo(0) } }, onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } })
                }
            },
            containerColor = SurfaceDark, shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun AddTaskScreen(viewModel: TugasViewModel, onSave: (String, String, String, String, String, List<SubTugas>) -> Unit) {
    var namaMatkul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("01") }
    var selectedMonth by remember { mutableStateOf("01") }
    var selectedYear by remember { mutableStateOf("2025") }

    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    // Filter hanya yang tidak deleted
    val activeKategoriTugas = kategoriTugasList.filter { !it.isDeleted }
    val activeKategoriMatkul = kategoriMatkulList.filter { !it.isDeleted }

    var kategoriTugas by remember { mutableStateOf(activeKategoriTugas.firstOrNull()?.nama ?: "") }
    var kategoriMatkul by remember { mutableStateOf(activeKategoriMatkul.firstOrNull()?.nama ?: "") }
    var subTugasList by remember { mutableStateOf<List<SubTugas>>(emptyList()) }

    var showKategoriTugasSelector by remember { mutableStateOf(false) }
    var showKategoriMatkulSelector by remember { mutableStateOf(false) }

    val currentTypeColor = Color(activeKategoriTugas.find { it.nama == kategoriTugas }?.warna ?: NeonCyan.toArgb())
    val currentCatColor = Color(activeKategoriMatkul.find { it.nama == kategoriMatkul }?.warna ?: NeonCyan.toArgb())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) { snackbarHostState.showSnackbar("Task saved!"); showSuccessSnackbar = false }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data, containerColor = NeonCyan, contentColor = Color.Black) } }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TaskFormFields(
                    namaMatkul = namaMatkul, onNamaChange = { namaMatkul = it },
                    deskripsi = deskripsi, onDeskripsiChange = { deskripsi = it },
                    selectedDay = selectedDay, selectedMonth = selectedMonth, selectedYear = selectedYear,
                    onDayChange = { selectedDay = it }, onMonthChange = { selectedMonth = it }, onYearChange = { selectedYear = it },
                    kategoriTugas = kategoriTugas, kategoriMatkul = kategoriMatkul,
                    currentTypeColor = currentTypeColor, currentCatColor = currentCatColor,
                    onTypeClick = { showKategoriTugasSelector = true }, onCategoryClick = { showKategoriMatkulSelector = true },
                    viewModel = viewModel, subTugasList = subTugasList,
                    onAddSubTugas = { subTugasList = subTugasList + SubTugas(nama = it) },
                    onRemoveSubTugas = { id -> subTugasList = subTugasList.filter { it.id != id } }
                )
                Button(
                    onClick = {
                        if (namaMatkul.isNotBlank()) {
                            onSave(namaMatkul, "$selectedDay-$selectedMonth-$selectedYear", kategoriTugas, kategoriMatkul, deskripsi, subTugasList)
                            namaMatkul = ""; deskripsi = ""; selectedDay = "01"; selectedMonth = "01"; selectedYear = "2025"; subTugasList = emptyList(); showSuccessSnackbar = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SAVE TASK", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }
            }
            ScrollArrowsOverlay(canScrollBackward = scrollState.canScrollBackward, canScrollForward = scrollState.canScrollForward, onUpClick = { scope.launch { scrollState.animateScrollTo(0) } }, onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } })
        }
    }

    if (showKategoriTugasSelector) {
        CategorySelectionDialog("SELECT TYPE", activeKategoriTugas, onSelected = { kategoriTugas = it.nama; showKategoriTugasSelector = false }, onDismiss = { showKategoriTugasSelector = false })
    }
    if (showKategoriMatkulSelector) {
        CategorySelectionDialog("SELECT CATEGORY", activeKategoriMatkul, onSelected = { kategoriMatkul = it.nama; showKategoriMatkulSelector = false }, onDismiss = { showKategoriMatkulSelector = false })
    }
}