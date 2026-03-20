package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun ManageCategoriesDialog(viewModel: TugasViewModel, onDismiss: () -> Unit) {
    // Hanya tampilkan yang tidak deleted
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()
    val activeKategoriTugas = kategoriTugasList.filter { !it.isDeleted }
    val activeKategoriMatkul = kategoriMatkulList.filter { !it.isDeleted }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("CLOSE", color = NeonCyan) }
            },
            title = { Text("MANAGE CATEGORIES", color = NeonCyan, fontWeight = FontWeight.Black) },
            text = {
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CategorySection(
                            title = "TASK TYPES",
                            list = activeKategoriTugas,
                            onAdd = { viewModel.tambahKategoriTugas(it) },
                            onDelete = { categoryToDelete = it; deleteType = "Task"; showDeleteConfirm = true },
                            placeholder = "New Type"
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        CategorySection(
                            title = "TASK CATEGORIES",
                            list = activeKategoriMatkul,
                            onAdd = { viewModel.tambahKategoriMatkul(it) },
                            onDelete = { categoryToDelete = it; deleteType = "Category"; showDeleteConfirm = true },
                            placeholder = "New Category"
                        )
                    }
                    ScrollArrowsOverlay(
                        canScrollBackward = scrollState.canScrollBackward,
                        canScrollForward = scrollState.canScrollForward,
                        onUpClick = { scope.launch { scrollState.animateScrollTo(0) } },
                        onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } }
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxHeight(0.85f)
        )
    }

    if (showDeleteConfirm && categoryToDelete != null) {
        val isTask = deleteType == "Task"
        DeleteConfirmationDialog(
            onDismiss = { showDeleteConfirm = false; categoryToDelete = null },
            onConfirm = {
                if (isTask) viewModel.hapusKategoriTugas(categoryToDelete!!)
                else viewModel.hapusKategoriMatkul(categoryToDelete!!)
                showDeleteConfirm = false
                categoryToDelete = null
            },
            title = if (isTask) "DELETE TASK TYPE" else "DELETE TASK CATEGORY",
            message = if (isTask)
                "Are you sure you want to delete the '${categoryToDelete}' task type? It will be moved to the archive."
            else
                "Are you sure you want to delete the '${categoryToDelete}' category? It will be moved to the archive."
        )
    }
}

@Composable
private fun CategorySection(
    title: String,
    list: List<Kategori>,
    onAdd: (Kategori) -> Unit,
    onDelete: (String) -> Unit,
    placeholder: String
) {
    var newName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(NeonCyan) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ColorSelector(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                placeholder = { Text(placeholder, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = {
                    if (newName.isNotBlank()) {
                        onAdd(Kategori(newName, selectedColor.toArgb()))
                        newName = ""
                    }
                },
                modifier = Modifier.background(selectedColor, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            }
        }
        list.forEach { kat ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(Color(kat.warna), CircleShape))
                    Text(kat.nama, color = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = { onDelete(kat.nama) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ColorSelector(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeonColors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (selectedColor == color) 2.dp else 0.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}