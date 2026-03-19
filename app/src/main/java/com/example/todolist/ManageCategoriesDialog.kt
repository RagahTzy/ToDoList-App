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
fun ManageCategoriesDialog(
    viewModel: TugasViewModel,
    onDismiss: () -> Unit
) {
    val kategoriTugasList by viewModel.kategoriTugasList.collectAsState()
    val kategoriMatkulList by viewModel.kategoriMatkulList.collectAsState()

    var newKategoriTugas by remember { mutableStateOf("") }
    var newKategoriMatkul by remember { mutableStateOf("") }
    var selectedColorTugas by remember { mutableStateOf(NeonCyan) }
    var selectedColorMatkul by remember { mutableStateOf(NeonCyan) }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Task Types Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("TASK TYPES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            ColorSelector(selectedColor = selectedColorTugas, onColorSelected = { selectedColorTugas = it })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newKategoriTugas,
                                    onValueChange = { newKategoriTugas = it },
                                    placeholder = { Text("New Type", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        if (newKategoriTugas.isNotBlank()) {
                                            viewModel.tambahKategoriTugas(Kategori(newKategoriTugas, selectedColorTugas.toArgb()))
                                            newKategoriTugas = ""
                                        }
                                    },
                                    modifier = Modifier.background(selectedColorTugas, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                }
                            }
                            kategoriTugasList.forEach { kat ->
                                CategoryListItem(
                                    kategori = kat,
                                    onDelete = {
                                        categoryToDelete = kat.nama
                                        deleteType = "Task"
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                        // Task Categories Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("TASK CATEGORIES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            ColorSelector(selectedColor = selectedColorMatkul, onColorSelected = { selectedColorMatkul = it })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newKategoriMatkul,
                                    onValueChange = { newKategoriMatkul = it },
                                    placeholder = { Text("New Category", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        if (newKategoriMatkul.isNotBlank()) {
                                            viewModel.tambahKategoriMatkul(Kategori(newKategoriMatkul, selectedColorMatkul.toArgb()))
                                            newKategoriMatkul = ""
                                        }
                                    },
                                    modifier = Modifier.background(selectedColorMatkul, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                }
                            }
                            kategoriMatkulList.forEach { kat ->
                                CategoryListItem(
                                    kategori = kat,
                                    onDelete = {
                                        categoryToDelete = kat.nama
                                        deleteType = "Category"
                                        showDeleteConfirm = true
                                    }
                                )
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
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(0.85f)
        )
    }

    if (showDeleteConfirm && categoryToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = {
                showDeleteConfirm = false
                categoryToDelete = null
            },
            onConfirm = {
                if (deleteType == "Task") viewModel.hapusKategoriTugas(categoryToDelete!!)
                else viewModel.hapusKategoriMatkul(categoryToDelete!!)
                showDeleteConfirm = false
                categoryToDelete = null
            },
            title = if (deleteType == "Task") "DELETE TASK TYPE" else "DELETE TASK CATEGORY",
            message = if (deleteType == "Task") {
                "Are you sure you want to delete the '$categoryToDelete' task type? This action cannot be undone."
            } else {
                "Are you sure you want to delete the '$categoryToDelete' category? This action cannot be undone."
            }
        )
    }
}

@Composable
private fun CategoryListItem(kategori: Kategori, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).background(Color(kategori.warna), CircleShape))
            Text(kategori.nama, color = Color.White.copy(alpha = 0.8f))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
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