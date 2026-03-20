package com.example.todolist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanTab(
    viewModel: TugasViewModel,
    onNavigateToDisplay: (String) -> Unit
) {
    val daftarTugas by viewModel.daftarTugas.collectAsState()
    val daftarCatatan by viewModel.daftarCatatan.collectAsState()
    val aktivTugas = daftarTugas.filter { !it.isCompleted }

    var selectedTugas by remember { mutableStateOf<Tugas?>(null) }
    var selectedSubTugas by remember { mutableStateOf<SubTugas?>(null) }
    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }
    var showTugasSelector by remember { mutableStateOf(false) }
    var showSubTugasSelector by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar("Note saved!")
            showSnackbar = false
        }
    }

    val filteredCatatan by remember(daftarCatatan, searchQuery) {
        derivedStateOf {
            val sorted = daftarCatatan.filter { !it.isDeleted }.sortedByDescending { it.timestamp }
            if (searchQuery.isBlank()) sorted
            else sorted.filter {
                it.judul.contains(searchQuery, ignoreCase = true) ||
                        it.isi.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = NeonCyan, contentColor = Color.Black)
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Form
                Text("ADD NOTE", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showTugasSelector = true },
                    color = SurfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedTugas?.namaMatkul?.uppercase() ?: "SELECT TASK",
                            color = if (selectedTugas != null) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = NeonCyan)
                    }
                }

                if (selectedTugas != null && selectedTugas!!.subTugasList.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showSubTugasSelector = true },
                        color = SurfaceDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSubTugas?.nama?.uppercase() ?: "SELECT SUB TASK (OPTIONAL)",
                                color = if (selectedSubTugas != null) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = NeonMagenta)
                        }
                    }
                }

                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Note Title", color = NeonCyan.copy(alpha = 0.6f)) },
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
                    value = isi,
                    onValueChange = { isi = it },
                    label = { Text("Note Content", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 5
                )

                Button(
                    onClick = {
                        if (selectedTugas != null && judul.isNotBlank() && isi.isNotBlank()) {
                            viewModel.tambahCatatan(
                                Catatan(
                                    tugasId = selectedTugas!!.id,
                                    subTugasId = selectedSubTugas?.id,
                                    judul = judul,
                                    isi = isi
                                )
                            )
                            judul = ""
                            isi = ""
                            selectedSubTugas = null
                            showSnackbar = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAVE NOTE", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }

                if (daftarCatatan.isNotEmpty()) {
                    HorizontalDivider(color = NeonCyan.copy(alpha = 0.2f))
                    Text("ALL NOTES", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search notes...", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
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

                    if (filteredCatatan.isEmpty()) {
                        Text("No notes found.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    } else {
                        filteredCatatan.forEach { catatan ->
                            val tugasNama = daftarTugas.find { it.id == catatan.tugasId }?.namaMatkul ?: "Task not found"
                            val subTugasNama = catatan.subTugasId?.let { subId ->
                                daftarTugas.find { it.id == catatan.tugasId }?.subTugasList?.find { it.id == subId }?.nama
                            }
                            CatatanListItem(
                                catatan = catatan,
                                tugasNama = tugasNama,
                                subTugasNama = subTugasNama,
                                onClick = { onNavigateToDisplay(catatan.id) },
                                onDelete = { viewModel.hapusCatatan(catatan.id) }
                            )
                        }
                    }
                }
            }
            ScrollArrowsOverlay(
                canScrollBackward = scrollState.canScrollBackward,
                canScrollForward = scrollState.canScrollForward,
                onUpClick = { scope.launch { scrollState.animateScrollTo(0) } },
                onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } }
            )
        }
    }

    if (showTugasSelector) {
        ImmersiveDialog(onDismissRequest = { showTugasSelector = false }) {
            AlertDialog(
                onDismissRequest = { showTugasSelector = false },
                confirmButton = {},
                title = { Text("SELECT TASK", color = NeonCyan, fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        aktivTugas.forEach { tugas ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedTugas = tugas
                                    selectedSubTugas = null
                                    showTugasSelector = false
                                },
                                color = SurfaceDark.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                            ) {
                                Text(tugas.namaMatkul, color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    if (showSubTugasSelector && selectedTugas != null) {
        ImmersiveDialog(onDismissRequest = { showSubTugasSelector = false }) {
            AlertDialog(
                onDismissRequest = { showSubTugasSelector = false },
                confirmButton = {},
                title = { Text("SELECT SUB TASK", color = NeonMagenta, fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedSubTugas = null
                                showSubTugasSelector = false
                            },
                            color = SurfaceDark.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Text("None (for main task only)", color = Color.Gray, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                        }
                        selectedTugas!!.subTugasList.forEach { sub ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedSubTugas = sub
                                    showSubTugasSelector = false
                                },
                                color = SurfaceDark.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.3f))
                            ) {
                                Text(sub.nama, color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun CatatanListItem(
    catatan: Catatan,
    tugasNama: String,
    subTugasNama: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateStr = remember(catatan.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(catatan.timestamp))
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(catatan.judul, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = if (subTugasNama != null) "$tugasNama › $subTugasNama" else tugasNama,
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(dateStr, color = Color.Gray, fontSize = 10.sp)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3366).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = { onDelete(); showDeleteConfirm = false },
            title = "DELETE NOTE",
            message = "Are you sure you want to delete the note '${catatan.judul}'? This action cannot be undone."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanDisplayScreen(
    catatanId: String,
    viewModel: TugasViewModel,
    onBack: () -> Unit
) {
    val daftarCatatan by viewModel.daftarCatatan.collectAsState()
    val daftarTugas by viewModel.daftarTugas.collectAsState()
    val catatan = daftarCatatan.find { it.id == catatanId }

    var isEditing by remember { mutableStateOf(false) }
    var editJudul by remember(catatan) { mutableStateOf(catatan?.judul ?: "") }
    var editIsi by remember(catatan) { mutableStateOf(catatan?.isi ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (catatan == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val tugasNama = daftarTugas.find { it.id == catatan.tugasId }?.namaMatkul ?: ""
    val subTugasNama = catatan.subTugasId?.let { subId ->
        daftarTugas.find { it.id == catatan.tugasId }?.subTugasList?.find { it.id == subId }?.nama
    }
    val dateStr = remember(catatan.timestamp) {
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(catatan.timestamp))
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                    }
                },
                title = {
                    Text("NOTE", fontWeight = FontWeight.Black, letterSpacing = 4.sp, color = NeonCyan)
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3366))
                        }
                    } else {
                        TextButton(onClick = {
                            if (editJudul.isNotBlank() && editIsi.isNotBlank()) {
                                viewModel.updateCatatan(catatan.copy(judul = editJudul, isi = editIsi))
                                isEditing = false
                            }
                        }) {
                            Text("SAVE", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = {
                            editJudul = catatan.judul
                            editIsi = catatan.isi
                            isEditing = false
                        }) {
                            Text("CANCEL", color = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = NeonCyan
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (subTugasNama != null) "$tugasNama › $subTugasNama" else tugasNama,
                color = NeonCyan.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(dateStr, color = Color.Gray, fontSize = 11.sp)

            HorizontalDivider(color = NeonCyan.copy(alpha = 0.2f))

            if (!isEditing) {
                Text(
                    text = catatan.judul,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                MarkdownText(
                    markdown = catatan.isi,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 26.sp
                    ),
                    linkColor = NeonCyan
                )
            } else {
                OutlinedTextField(
                    value = editJudul,
                    onValueChange = { editJudul = it },
                    label = { Text("Title", color = NeonCyan.copy(alpha = 0.6f)) },
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
                    value = editIsi,
                    onValueChange = { editIsi = it },
                    label = { Text("Note Content", color = NeonCyan.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 8
                )
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                viewModel.hapusCatatan(catatanId)
                showDeleteConfirm = false
                onBack()
            },
            title = "DELETE NOTE",
            message = "Are you sure you want to delete the note '${catatan.judul}'? This action cannot be undone."
        )
    }
}