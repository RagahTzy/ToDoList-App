package com.example.todolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material.icons.automirrored.filled.Notes

private fun isDeadlineNear(deadline: String): Boolean = try {
    ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd-MM-yyyy"))) <= 1L
} catch (e: Exception) { false }

@Composable
private fun MuteButton(muted: Boolean, onClick: () -> Unit) {
    val color = if (muted) Color.Gray else NeonRed
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (muted) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = color
            )
            Text(
                text = if (muted) "NOTIF OFF" else "BERHENTI NOTIF",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TugasCardNeon(
    tugas: Tugas,
    baseColor: Color,
    catColor: Color,
    catatanList: List<Catatan>,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleMute: () -> Unit,
    onComplete: () -> Unit,
    onToggleSubTugas: (String) -> Unit,
    onCatatanClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")
    val isNearDeadline = remember(tugas.deadline) { isDeadlineNear(tugas.deadline) }
    val accentColor = if (isNearDeadline) NeonRed else baseColor

    val hasSubTugas = tugas.subTugasList.isNotEmpty()
    val completedCount = tugas.subTugasList.count { it.isCompleted }
    val progress = if (hasSubTugas) completedCount.toFloat() / tugas.subTugasList.size else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    // Catatan untuk tugas utama (bukan sub tugas)
    val catatanTugasUtama = catatanList.filter { it.subTugasId == null }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
            .border(
                BorderStroke(2.dp, accentColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.1f)),
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
                        color = if (isNearDeadline) NeonRed else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onComplete) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = NeonGreen)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3366))
                    }
                }
            }

            if (hasSubTugas) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PROGRESS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor, letterSpacing = 1.sp)
                    Text("$completedCount/${tugas.subTugasList.size}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f)
                )
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
                    NeonBadge(text = tugas.kategoriTugas, color = baseColor)
                    NeonBadge(text = tugas.kategoriMatkul, color = catColor)
                    if (isNearDeadline) MuteButton(muted = tugas.reminderMuted, onClick = onToggleMute)
                }
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand", tint = baseColor, modifier = Modifier.rotate(rotation))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = baseColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Deskripsi
                    Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = baseColor, fontWeight = FontWeight.Black)
                    Text(
                        text = tugas.deskripsi.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Sub Tugas
                    if (hasSubTugas) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SUB TASKS", style = MaterialTheme.typography.labelSmall, color = baseColor, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        tugas.subTugasList.forEach { sub ->
                            val catatanSubTugas = catatanList.filter { it.subTugasId == sub.id }
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (sub.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (sub.isCompleted) NeonGreen else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp).clickable { onToggleSubTugas(sub.id) }
                                    )
                                    Text(
                                        text = sub.nama,
                                        fontSize = 13.sp,
                                        color = if (sub.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.8f),
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.clickable { onToggleSubTugas(sub.id) }
                                    )
                                }
                                if (catatanSubTugas.isNotEmpty()) {
                                    catatanSubTugas.forEach { catatan ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onCatatanClick(catatan.id) }
                                                .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Notes,
                                                contentDescription = null,
                                                tint = NeonMagenta.copy(alpha = 0.8f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = catatan.judul,
                                                fontSize = 11.sp,
                                                color = NeonMagenta.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Catatan tugas utama
                    if (catatanTugasUtama.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("MAIN NOTES", style = MaterialTheme.typography.labelSmall, color = NeonMagenta, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        catatanTugasUtama.forEach { catatan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCatatanClick(catatan.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = NeonMagenta.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                Text(catatan.judul, fontSize = 13.sp, color = NeonMagenta.copy(alpha = 0.9f), modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonMagenta.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}