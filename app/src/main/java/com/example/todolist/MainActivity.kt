package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.ToDoListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TugasViewModel by viewModels { TugasViewModelFactory(applicationContext) }

        setContent {
            ToDoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
            if (searchQuery.isEmpty()) {
                daftarTugas
            } else {
                daftarTugas.filter {
                    it.namaMatkul.contains(searchQuery, ignoreCase = true) ||
                            it.kategoriTugas.contains(searchQuery, ignoreCase = true) ||
                            it.kategoriMatkul.contains(searchQuery, ignoreCase = true) ||
                            it.deadline.contains(searchQuery)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencatatan Tugas") },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Urutkan")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("Urut Nama") }, onClick = { viewModel.sortByNama(); showSortMenu = false })
                            DropdownMenuItem(text = { Text("Urut Deadline") }, onClick = { viewModel.sortByDeadline(); showSortMenu = false })
                            DropdownMenuItem(text = { Text("Urut Kat. Tugas") }, onClick = { viewModel.sortByKategoriTugas(); showSortMenu = false })
                            DropdownMenuItem(text = { Text("Urut Kat. Matkul") }, onClick = { viewModel.sortByKategoriMatkul(); showSortMenu = false })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedTugas = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
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
                    .padding(8.dp),
                placeholder = { Text("Cari tugas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTugas, key = { it.id }) { tugas ->
                    TugasCard(
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
fun TugasCard(tugas: Tugas, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tugas.namaMatkul, style = MaterialTheme.typography.titleLarge)
                Text(text = "Deadline: ${tugas.deadline}", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    AssistChip(onClick = {}, label = { Text(tugas.kategoriTugas, fontSize = 12.sp) })
                    AssistChip(onClick = {}, label = { Text(tugas.kategoriMatkul, fontSize = 12.sp) })
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2196F3))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFF44336))
                }
            }
        }
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
        title = { Text(if (tugas == null) "Tambah Tugas" else "Update Tugas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = namaMatkul,
                    onValueChange = { namaMatkul = it },
                    label = { Text("Nama Matkul") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Deadline (Tgl - Bln - Thn):", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimpleDropdown(
                        options = viewModel.days,
                        selected = selectedDay,
                        onSelected = { selectedDay = it },
                        modifier = Modifier.weight(1f)
                    )
                    SimpleDropdown(
                        options = viewModel.months,
                        selected = selectedMonth,
                        onSelected = { selectedMonth = it },
                        modifier = Modifier.weight(1f)
                    )
                    SimpleDropdown(
                        options = viewModel.years,
                        selected = selectedYear,
                        onSelected = { selectedYear = it },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Text("Kategori Tugas:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Kelompok", "Individu").forEach {
                        FilterChip(
                            selected = kategoriTugas == it,
                            onClick = { kategoriTugas = it },
                            label = { Text(it) }
                        )
                    }
                }

                Text("Kategori Matkul:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Praktikum", "Teori").forEach {
                        FilterChip(
                            selected = kategoriMatkul == it,
                            onClick = { kategoriMatkul = it },
                            label = { Text(it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(namaMatkul, "$selectedDay-$selectedMonth-$selectedYear", kategoriTugas, kategoriMatkul)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
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
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selected, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp) // Membatasi tinggi agar tidak lag
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}